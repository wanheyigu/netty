package com.zxy.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
/**
 * 异常的传递是按照处理器装载到pipeline中的顺序，依次向后查找传递，不论Inbound还是Outbound。
 */
public class ChannelInboundHandle1 extends ChannelInboundHandlerAdapter{

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("read msg:"+msg);

		throw new ArrayIndexOutOfBoundsException("111-Inbound 处理器异常");
	}
	
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		
		System.out.println("111-Inbound");
		ctx.fireExceptionCaught(cause);
	}
}
