package com.zxy.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
/*
 * 需求：不需要读取数据，所有处理器实现ChannelInboundHandlerAdapter
 */
public class SomeClientHandler extends ChannelInboundHandlerAdapter{
	
	private String msg = "hello world!";;
	
	
	
	//发送数据
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		byte[] massage = msg.getBytes();
		ByteBuf buffer = null;
		
		for(int i=0;i<100;i++) {
			buffer = Unpooled.buffer(massage.length);
			buffer.writeBytes(massage);
			ctx.writeAndFlush(buffer);
			//ctx.writeAndFlush(msg);
		}
		
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
	}

}
