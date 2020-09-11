package com.zxy.rpc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.zxy.bean.InvokeMessage;
import com.zxy.service.SomeService;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
/**客户端代理类
 * 
 */
public class RpcProxy {

	/*
	 * 编写框架不可使用具体类型,要应用泛型
	 * public static SomeService create(Class<SomeService> class1) 
	 * 
	 * 并且在编写框架要使用泛型方法，即,加上 <T>,如果不加<T>这种：
	 * public static T create(Class<?> clazz) 是在编译的时候检查；
	 * 加 <T> 这种：public static <T> T create(Class<?> clazz) 
	 * 是在运行的时候检查泛型,即泛型检查后移;(编写框架约定使用加 <T>)
	 */
	
	@SuppressWarnings("unchecked")
	public static <T> T create(Class<?> clazz) {
		/*
		 * 此处创建的代理对象为JDK proxy:
		 *    newProxyInstance(ClassLoader loader,Class<?>[] interfaces,InvocationHandler h)
		 *    类加载器,通过传入的接口即可获取  @param   loader the class loader to define the proxy class
	     *    接口数组,传入的clazz接口  @param   interfaces the list of interfaces for the proxy class
	     *             to implement
	     *    调用处理器,一般用匿名内部类定义  @param   h the invocation handler to dispatch method invocations to
		 */
		return (T) Proxy.newProxyInstance(clazz.getClassLoader(), 
				new Class[]{clazz}, 
				new InvocationHandler() {
					
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						/* 前置判断过滤：
						 * 若调用的是Object中的方法，则直接进行本地调用即可
						 */
						if(Object.class.equals(method.getDeclaringClass())) {
							/*
							 * 本地调用
							 * 注：此处应用this，this在此处是匿名内部类对象"new InvocationHandler()",
							 * 而不是RpcProxy,所以可以应用，不会报错。
							 * 思考：此处的this是否可以替换成为invoke(Object proxy,......)出入的proxy对象？
							 *     不可以，因为他们代表这两个不同的对象，此处的this是匿名内部类对象new InvocationHandler()，
							 *     而Object proxy代表的是create(Class<?> clazz)方法生产的代理对象，即，
							 *     SomeConsumer中的“SomeService service = RpcProxy.create(SomeService.class);”
							 */
							
							return method.invoke(this, args);
						}
						
						/* 关于invoke(Object proxy,...) 中proxy的使用场景
						 * 假设service接口中有方法md；
						 * 
						 * if("md".equals(method.getName())) {
							// ......执行一些非调用该方法的操作
							//此处不调用该方法，只是想返回该调用对象本身，那么就可以返回proxy；
							//返回的proxy就是service接口本身
							return proxy;
							
						}
						 */
						
						
						
						// 进行远程调用
						return rpcInvoke(clazz,method,args);
					}
				});
	}

	private static Object rpcInvoke(Class<?> clazz,Method method, Object[] args) throws Exception {
		//将自定义处理器定义在此，用于服务端执行方法后获取发送过来的远程调用处理结果
		RpcClientHandler rpcClientHandler = new RpcClientHandler();
		/* 远程调用方法：
		 * 首先通过netty连接远程服务端
		 */
		EventLoopGroup group = new NioEventLoopGroup();
		
		try {
			Bootstrap bootstrap =  new Bootstrap();
			
			bootstrap.group(group)
					 .channel(NioSocketChannel.class)
					 /* Tcp  Nagle算法： 禁用option(ChannelOption.TCP_NODELAY, true)，默认是false应用算法；
					  * tcp的nagle算法是延迟发送，每次发送数据会根据当前网络主机情况攒够足够大再一次性发送，
					  * 这种算法解决方案是针对发送数据比较多，比较散的情况下应用比较高效，会提高吞吐量，提高发送速度；
					  * 
					  * 但是，当前需求场景时远程调用，就是一个invokeMessage小数据，因此关闭延迟，有数据立即发出。
					  */
					 .option(ChannelOption.TCP_NODELAY, true) 
					 .handler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							
							ch.pipeline().addLast("ObjectEncoder",new ObjectEncoder());
							ch.pipeline().addLast("ObjectDecoder",new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
							
							ch.pipeline().addLast("RpcClientHandler",rpcClientHandler);
						}
					});
			ChannelFuture future = bootstrap.connect("localhost", 8001).sync();
			
			System.out.println("连接到Server");
			
			/*以下内容可以在自定义处理器的ChannelActive方法中实现*/
			//创建并初始化调用信息
			InvokeMessage message = new InvokeMessage();
			message.setClassName(clazz.getName());
			message.setMethodName(method.getName());
			message.setParamTypes(method.getParameterTypes());
			message.setParamValues(args);
			
			//发送调用请求信息给Server
			future.channel().writeAndFlush(message).sync();
			/*以上内容可以在自定义处理器的ChannelActive方法中实现*/
			
			future.channel().closeFuture().sync();
		}finally {
			group.shutdownGracefully();
		}
		
		/*
		 * 此处返回远程调用的执行结果:
		 *    执行结果result为自定义处理器RpcClientHandler中触发的channelRead0方法中的msg,
		 *    因此在RpcClientHandler中定义getResult()方法获取；
		 * 
		 */
		return rpcClientHandler.getResult();
	}

}
