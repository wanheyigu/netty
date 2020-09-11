package com.zxy.rpc_client.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.zxy.rpc_api.pojo.InvokeMessage;
import com.zxy.rpc_api.service.SomeService;
import com.zxy.rpc_client.client.discovery.ServiceDiscovery;
import com.zxy.rpc_client.client.discovery.ServiceDiscoveryImpl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

@Component
public class RpcProxy {
	
	@Autowired
	private ServiceDiscovery serviceDiscovery;
	
	//编写框架需要用泛型
	@SuppressWarnings("unchecked")
	//public static <T> T creat(final Class<?> clazz) {
	public <T> T creat(final Class<?> clazz) {
		/*
		 *  使用JDKproxy创建实例
		 *      (参数一：类加载器使用出入接口的类加载器，
		 *       参数二：接口(类数组)创建类数组，传入接口
		 *       参数三：调用处理器new一个调用处理器，实现默认调用方法)
		 */
		return (T) Proxy.newProxyInstance(clazz.getClassLoader(), 
				new Class[] {clazz}, 
				new InvocationHandler() {

					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						// 判断是否为Object方法，如果是直接本地调用
						if(Object.class.equals(method.getDeclaringClass())) {
							return method.invoke(this, args);
						}
						
						//如果是远程调用，则调用远程调用方法
						return rpcInvoke(clazz,method,args);
						
						
					}
			
		});
	}

	//protected static Object rpcInvoke(Class<?> clazz, Method method, Object[] args) throws Exception {
	public Object rpcInvoke(Class<?> clazz, Method method, Object[] args) throws Exception {
		// TODO Auto-generated method stub
		
		final RpcClientHandler rpcClientHandler = new RpcClientHandler();
		
		EventLoopGroup group = new NioEventLoopGroup();
		
		Bootstrap bootstrap = new Bootstrap();
		
		try {
			bootstrap.group(group)
					 .channel(NioSocketChannel.class)
					 .option(ChannelOption.TCP_NODELAY, true)
					 .handler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							// TODO Auto-generated method stub
							ch.pipeline().addLast(new ObjectEncoder());
							ch.pipeline().addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
							
							//自定义业务处理器，用于接收服务端处理远程调用的结果
							ch.pipeline().addLast(rpcClientHandler);
						}
						 
					 });
			/*
			 * 连接服务器地址
			 *    服务器地址是由服务发现获取到；
			 */
			//ServiceDiscoveryImpl discoveryImpl = new ServiceDiscoveryImpl();
			/*discoveryImpl有可能出现null情况，因此要防止空指针异常*/
			if(serviceDiscovery == null) {
				return null;
			}
			//通过出入接口名称获取服务地址
			String serviceAdress = serviceDiscovery.discovery(clazz.getName());
			//解析服务地址
			String ip = serviceAdress.split(":")[0];
			String portStr = serviceAdress.split(":")[1];
			
			ChannelFuture future = bootstrap.connect(ip, Integer.valueOf(portStr)).sync();
			System.out.println("linked server！");
			
			/*
			 * 连接上数据库后开始发送远程调用请求
			 */
			//1.封装请求对象InvokeMessage
			InvokeMessage msg = new InvokeMessage();
			msg.setProviderName(clazz.getName());
			msg.setMethodName(method.getName());
			msg.setParamTypes(method.getParameterTypes());
			msg.setParamValues(args);
			
			future.channel().writeAndFlush(msg).sync();
			
			future.channel().closeFuture().sync();
		}finally {
			group.shutdownGracefully();
		}
		
		
		return rpcClientHandler.getResult();
	}

}
