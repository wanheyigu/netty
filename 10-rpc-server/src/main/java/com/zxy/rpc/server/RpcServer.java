package com.zxy.rpc.server;

import java.io.File;
import java.io.StreamCorruptedException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/** rpc-server端
 * 即服务提供者-provider
 */
public class RpcServer {
	
	/**
	 * 关于线程安全的考虑即采用那种list和map进行存储：
	 *    此处不用考虑线程安全问题，因为当服务器启动时所有的类名会加载到集合中，此后不会在改变；
	 *    并非一般成员变量，在程序运行过程中会出现变化，可能会产生线程不安全；而且线程安全的集合
	 *    或应用在互联网项目中要根据业务场景慎用，因一旦保障线程安全，那么就要牺牲效率，很容易
	 *    在高并发场景下出现问题。
	 * 注：此RPC的开发不考虑动态加载服务的功能，如有动态加载就要重新考虑线程安全问题。
	 */
	
	/** 指定包集合
	 * 用于存储指定包下所有接口实现类的类名
	 * 线程安全的list
	 * private List<String> classCache = Collections.synchronizedList(new ArrayList<>());
	 * 线程不安全的list
	 */
	private List<String> classCache = new ArrayList<>();
	
	/** 定义注册中心(注册表)
	 * 1.key为服务名称，即业务接口名(指定包下类的全限定性类名)
	 * 2.value为对应接口实现类的实例(由key通过反射机制创建)
	 * 线程安全的map
	 * private ConcurrentHashMap map;
	 * 线程不安全的map
	 */
	private Map<String,Object> registryMap = new HashMap<>();
	
	
	/** 服务发布：将服务名称与对应包中实现类实例的映射关系(map)写入到注册中心的过程。
	 * 发布指定包：com.zxy.rpc.service下的所有服务(实现类)
	 * 发布功能说明：
	 *    将指定包下的实现类的实例(对象)创建完成，然后把实例与其对应的名称
	 *    建立一对一关系，最后将其写到注册中心，即完成发布流程。
	 */
	public void publish(String providerPackage) {
		//1.将指定包下的所有实现类名称写入到classCache集合中
		getProviderClass(providerPackage);
		
		//2.将服务提供者注册到注册表
		doRegister();
	}

	/**
	 * 1.将指定包下的所有实现类名称写入到classCache集合中
	 * @param providerPackage 指定包路径
	 */
	private void getProviderClass(String providerPackage) {
	//public void getProviderClass(String providerPackage) {//单元测试时放开
		/* 将指定包转化为对象资源
		 * 1-将com.zxy.rpc.service 转为 com\zxy\rpc\service
		 *   providerPackage.replaceAll("\\.", "/")
		 * 2-通过类加载器加载包下.class文件
		 *   this.getClass().getClassLoader().getResource() 
		 */
		URL resource =  this.getClass().getClassLoader().getResource(providerPackage.replaceAll("\\.", "/"));
		//System.out.println("server-resource----->"+resource.toString());
		
		//URL对象不易操作，将其转为file
		File dir = new File(resource.getFile());
		//遍历dir目录中的所有文件，查找.class文件
		for(File file : dir.listFiles()) {
			/* 首先要判断dir下的当前元素是否为目录(即指定包service中存在子目录)*/
			if(file.isDirectory()) {
				/*如果当前元素为子目录，调用递归*/
				getProviderClass(providerPackage+"."+file.getName());
			}else {
				/* 如元素为文件(.class文件)，那么直接存入classCache集合中
				 * 注,存入过程要去除扩展名,获取到简单类名；
				 */
				String fileName = file.getName().replace(".class", "").trim();
				/** 存入classCache的类名思考：
				 *  以上获取到了指定包下的简单类名，但是classCache中只存入简单类名是不符合逻辑且不可用的，
				 *  因为后期注册中心需要通过反射来创建类的实例，那么就需要《全限定性类名》即包名加类名才能完成
				 *  反射创建，因此此处存入classCache集合中的应该是全限定性类名。
				 */
				classCache.add(providerPackage+"."+fileName);
			}
		}
		//System.out.println(classCache);
	}
	/**
	 * 2.将服务提供者注册到注册表
	 */
	private void doRegister() {
		/*
		 * 若没有提供者，无需注册:
		 * 为提升效率，在classCache中没有数据的情况下不在向下运行;
		 */
		if(classCache.size() == 0) return;
		
		/** 将服务注册到注册中心过程：
		 *  遍历classCache，获取到实现类所实现的所有接口名称(全限定性类名)，
		 *  通过名称创建该实现类对应的实例对象
		 */
		for(String className : classCache) {
			try {
				//加载当前遍历的类
				Class<?> clazz = Class.forName(className);
				/* 将服务注册到注册中心
				 * key:获取接口名->clazz.getInterfaces()[0].getName(),做为服务提供者提供的服务实现类只能实现一个接口；
				 * value:创建接口实例->clazz.newInstance();
				 * 将key[接口]及value[对应的实例]存入registryMap。
				 */
				
				//System.out.println("接口名称："+clazz.getInterfaces()[0].getName());
				//System.out.println("接口实例："+clazz.newInstance());
				
				registryMap.put(clazz.getInterfaces()[0].getName(), clazz.newInstance());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * 启动服务器
	 */
	public void start() throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workGroup = new NioEventLoopGroup();
		
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workGroup)
					 .option(ChannelOption.SO_BACKLOG, 1024) //设置临时队列
					 .childOption(ChannelOption.SO_KEEPALIVE, true)//维持长连接
					 .channel(NioServerSocketChannel.class)
					 .childHandler(new ChannelInitializer<SocketChannel>(){
	
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							
							ChannelPipeline pipeline = ch.pipeline();
							/* 如何添加处理器，即添加什么处理器？
							 * 需求是"客户端向服务端发送一个带有请求参数的调用请求",这类请求参数是要调用某个服务的请求信息，
							 * 封装成一个Object的InvokeMessage对象,但是发送过来的是ByteBuf,所以此处需要将ByteBuf
							 * 解码为InvokeMessage对象，因此需要添加对象解码器。
							 * 采用----->ObjectDecoder(maxObjectSize, classResolver)
							 * Creates a new decoder with the specified maximum object size.
						     *
						     * @param maxObjectSize  the maximum byte length of the serialized object.
						     *                       if the length of the received object is greater
						     *                       than this value, {@link StreamCorruptedException}
						     *                       will be raised.
						     *                       设置序列化对象的最大长度，如果接收的长度大于设置就会抛出异常；(在不
						     *                       知道对象长度的情况下，设置为Integer.MAX_VALUE)
						     * @param classResolver    the {@link ClassResolver} which will load the class
						     *                       of the serialized object
						     *                       类解析器，此类解析器将加载要序列化的类；(加载序列化的InvokeMessage类到内存)
						     *                       使用ClassResolvers.cacheDisabled(null)
						     *                          cache disabled  不应用缓存
													    * @param classLoader - specific classLoader to use, or null if you want to revert to default
													    					   使用自定义类加载器，如果要使用默认类加载器就设置为null;
													    * @return new instance of class resolver
							 */
							pipeline.addLast("ObjectDecoder",
									new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
							//服务端要向客户端发送处理结果(也是Object)，所以要添加对象编码器；
							pipeline.addLast("ObjectEncoder",new ObjectEncoder());
							//自定义业务处理器
							pipeline.addLast("RpcServerHandler",new RpcServerHandler(registryMap));
						}
						 
					 });
			ChannelFuture future = bootstrap.bind(8001).sync();
			System.out.println("服务器已启动:8001");
			
			/* closeFuture()是当channel被关闭的时候会触发此方法，
			 * 如果添加监听的话，会触发监听的回调方法执行。
			 * 所以此处使用closeFuture()实际是监听close()方法。
			 */
			future.channel().closeFuture().sync();
		}finally {
			bossGroup.shutdownGracefully();
			workGroup.shutdownGracefully();
		}
		
	}

}
