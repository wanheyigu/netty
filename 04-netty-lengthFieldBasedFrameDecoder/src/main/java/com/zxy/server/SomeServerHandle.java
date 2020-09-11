package com.zxy.server;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class SomeServerHandle extends ChannelInboundHandlerAdapter{

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		//将来自与客户端的数据显示在服务端控制台
		System.out.println(ctx.channel().remoteAddress()+"----->"+msg);
		//向客户端发送数据
		ctx.channel().writeAndFlush("from Server:"+UUID.randomUUID());
		//休眠1秒钟
		TimeUnit.MILLISECONDS.sleep(1000);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		
		ctx.close();
	}
}
