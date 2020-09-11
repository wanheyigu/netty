package com.zxy.rpc.server;

import java.util.Map;

import com.zxy.bean.InvokeMessage;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 自定义业务处理器：
 * 接收客户端发送的请求参数对象InvokeMessage,完成服务端调用后,
 * 将请求处理结果发送会客户端,其中InvokeMessage不需要返回,因此
 * 继承simpleChannelInboundHandler(自动释放请求数据),即不需要保留请求数据;
 * 
 * InvokeMassage请求参数对象是服务端与客户端都需要的封装对象，因此创建api工程，
 * 并在api工程中定义InvokeMassage类，在通过依赖引入的方式在服务端和客户端使用。
 * 
 * 服务端需要通过InvokeMessage请求获取的信息，来进行反射调用方法。
 */
public class RpcServerHandler extends SimpleChannelInboundHandler<InvokeMessage>{

	//通过构造器注入,注册中心
	private Map<String,Object> registryMap;
	
	public RpcServerHandler(Map<String, Object> registryMap) {
		this.registryMap = registryMap;
	}



	@Override
	protected void channelRead0(ChannelHandlerContext ctx, InvokeMessage msg) throws Exception {
		//如果InvokeMessage请求的服务在注册中心中不存在，那么直接返回result;
		Object result = "没有指定的提供者";
		/*
		 * 1.判断注册中心是否有InvokeMessage请求的服务
		 */
		if(registryMap.containsKey(msg.getClassName())) {
			/*
			 * 2.注册中心存在InvokeMessage请求的服务,
			 *   那么,通过InvokeMessage请求中的服务名称
			 *   在注册中心获取provider(服务提供者)实例;
			 */
			Object provider = registryMap.get(msg.getClassName());
			/*
			 * 3.(反射)执行provider中InvokeMessage请求的方法,并返回执行结果
			 */
			result = provider.getClass()
					.getMethod(msg.getMethodName(), msg.getParamTypes())
					.invoke(provider, msg.getParamValues());
		}
		/*
		 * 5.将调用结果发送给客户端
		 */
		ctx.writeAndFlush(result);
		
		ctx.close();
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

}
