package com.zxy.rpc_server.server;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zxy.rpc_server.registry.RegistryCenter;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class RpcServer {

	//存储指定包下类名集合
	private List<String> classCache = new ArrayList<>();
	
	//注册中心，key存储接口名称(全限定性名)，value存储实现接口的实例(一般框架一个接口只允许有一个实现，多实现需要通过version控制)
	private Map<String,Object> registerMap = new HashMap<>();
	
	private String serviceAddress;

	/*
	 * 完成扫描与注册工作:
	 *    将服务与服务提供者注册到注册中心ZK中；
	 */
	public void publishService(RegistryCenter registryCenter,String serviceAddress,String providerPackage) {
		//扫描指定包，生成服务名集合classCache
		getProvider(providerPackage);
		
		//将服务及服务提供者注册到注册中心registerCenter
		doRegister(registryCenter,serviceAddress);
		
		this.serviceAddress = serviceAddress;
		
	}
	
	private void getProvider(String providerPackage) {
		
		// 将指定包以对象获取,需要修改包路径中的"."为"\"
		URL resource = this.getClass().getClassLoader().getResource(providerPackage.replaceAll("\\.", "/"));
		
		//将包对象类型转为File
		File dir = new File(resource.getFile());
		
		//遍历dir对象，获取包下文件对象
		for (File file : dir.listFiles()) {
			//若当前遍历的文件是目录(即包下的子包)则递归调用
			if(file.isDirectory()) {
				getProvider(providerPackage+"."+file.getName());
			}else {
				//获取文件的名称，需要去除后缀.class
				String className = file.getName().replace(".class", "");
				
				//若当前遍历为文件则将全限定性名出入集合classCache
				classCache.add(providerPackage+"."+className);
				//System.out.println(providerPackage+"."+className);
			}
		}
	}

	//注册
	private void doRegister(RegistryCenter registryCenter,String serviceAddress) {
		// 判断集合是否为空，如果为空则返回，不在向下执行
		if(classCache.size() == 0) return;
		
		//遍历classCache，通过集合中全限定性类名获取接口名称做为key存入Map，获取其实例出入value
		for (String className : classCache) {
			try {
				//通过反射获取类
				Class<?> clazz = Class.forName(className);
				/*
				 * 注册到注册表:
				 * 	  key:接口名称
				 * 	  value:实现类实例
				 * --执行的时候应用的还是注册表中的实例
				 */
				registerMap.put(clazz.getInterfaces()[0].getName(), clazz.newInstance());
				
				/*
				 * 注册到注册中心:向ZK中注册服务提供者
				 *    目的是对外暴露提供者主机，客户端可以通过服务名称获取可提供服务的主机列表，
				 *    然后通过选定的服务主机获取其注册表中提供的实现类实例，运行实例完成远程调用；
				 *    因此两步注册必不可少；
				 *    
				 * 注：此处有一待解决问题，如果已经注册过一次，那么在发起注册，如何解决，当然在ZKRegistryCenterImpl
				 *    中可以增加判断(即创建前判断是否存在，而不用执行create()抛出异常创建失败，提升一部分效率)或不加判断
				 *    直接由执行create()抛出异常创建失败，可以解决，但是相对效率低，判断需要通过网络请求ZK，而如果在此处
				 *    进行判断过滤则不需要消耗网络资源，相对效率提升。
				 * 思考：此处的RpcServer并不是交由spring管理的，未应用spring，所以其是一个多例对象，每次应用都会被创建
				 *     ，因此此处不好使用全局Map解决，只能暂时交由ZKRegistryCenterImpl去判断解决。
				 */
				registryCenter.register(clazz.getInterfaces()[0].getName(), serviceAddress);
				
				
				
				System.out.println("接口名称："+clazz.getInterfaces()[0].getName());
				System.out.println("实例对象："+clazz.newInstance());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//启动服务
	public void starter() throws Exception {
		
		EventLoopGroup parentGroup = new NioEventLoopGroup();
		EventLoopGroup childGroup = new NioEventLoopGroup();
		
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			
			bootstrap.group(parentGroup, childGroup)
					 .channel(NioServerSocketChannel.class)
					 .option(ChannelOption.SO_BACKLOG, 1024)
					 .option(ChannelOption.SO_KEEPALIVE, true)
					 .childHandler(new ChannelInitializer<SocketChannel>(){

						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							// TODO Auto-generated method stub
							ch.pipeline().addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
							ch.pipeline().addLast(new ObjectEncoder());
							//自定义业务处理器
							ch.pipeline().addLast(new RpcServerHandler(registerMap));
						}
						 
					 });
			
			String ip = serviceAddress.split(":")[0];
			String postStr = serviceAddress.split(":")[1];
			
			ChannelFuture future = bootstrap.bind(ip,Integer.valueOf(postStr)).sync();
			
			System.out.println("server is coming,on the ----->" + serviceAddress);
			
			future.channel().closeFuture().sync();
			
		}finally {
			parentGroup.shutdownGracefully();
			childGroup.shutdownGracefully();
		}
		
	}
	
}
