package com.zxy.rpc_client.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
/**
 * server端发送的远程调用处理结果为Object类型，因此
 * SimpleChannelInboundHandler<参数> 为 Object
 */
public class RpcClientHandler extends SimpleChannelInboundHandler<Object>{

	//定义私有结果变量
	private Object result;
	
	//定义get方法，使外部可以获取结果
	public Object getResult() {
		return this.result;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		// TODO Auto-generated method stub
		this.result = msg;
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

}
