package com.zxy.rpc_server.server;

import java.util.Map;

import com.zxy.rpc_api.pojo.InvokeMessage;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
/**
 * 1.业务需求不需要放回客户端发送的数据，因此继承SimpleChannelInboundHandler(自动释放)
 * 2.SimpleChannelInboundHandler<参数>，客户端发送的是InvokeMessage对象，因此此处处理的数据参数即InvokeMessage
 *
 */
public class RpcServerHandler extends SimpleChannelInboundHandler<InvokeMessage>{

	private Map<String,Object> registerCenter;
	
	public RpcServerHandler(Map<String,Object> registerCenter) {
		this.registerCenter = registerCenter;
	}
	
	
	
	// 接收msg中服务名称匹配注册中心中是否存在，如存在就调用，若不存在向客户端发送服务不存在
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, InvokeMessage msg) throws Exception {
		Object resulet = "没有此服务";
		
		if(registerCenter.containsKey(msg.getProviderName())) {
			//获取实例
			Object provider = registerCenter.get(msg.getProviderName());
			
			System.out.println("实例："+provider.getClass()+"\n"+
							   "方法："+msg.getMethodName()+"\n"+
							   "参数类型："+msg.getParamTypes()+"\n"+
							   "参数："+msg.getParamValues());
			
			//若存在此服务则调用
			resulet = provider.getClass()
							  .getMethod(msg.getMethodName(), msg.getParamTypes())
							  .invoke(provider, msg.getParamValues());
		}
		
		ctx.writeAndFlush(resulet);
		ctx.close();
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

}
