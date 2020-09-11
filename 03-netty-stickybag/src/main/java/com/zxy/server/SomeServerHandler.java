package com.zxy.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/*
 * 需求：发送方发送大数据包，需要自动释放，所有实现SimpleChannelInboundHandler
 */
public class SomeServerHandler extends SimpleChannelInboundHandler<String>{
	//计数器
	private int counter;
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

		System.out.println("server端接收到的第["+ ++ counter +"]个数据包:"+msg);
		
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
	}

}
