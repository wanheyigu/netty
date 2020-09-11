package com.zxy.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
/**
 * 自定义处理器继承ChannelInboundHandlerAdapter
 */
public class ChannelInboundHandle2 extends ChannelInboundHandlerAdapter{

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		
		System.out.println("222-Inbound");
		ctx.fireExceptionCaught(cause);
	}
	
}
