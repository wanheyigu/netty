package com.zxy.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
/**
 * 自定义处理器继承ChannelInboundHandlerAdapter
 */
public class ChannelInboundHandle2 extends ChannelInboundHandlerAdapter{

	/*
	 * 实现向下传递功能
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		System.out.println("ChannelInboundHandle2----->"+msg);
		
		//触发下一个结点(处理器)的channelRead(args...)方法
		ctx.fireChannelRead(msg);
		//super.channelRead(ctx, msg);
	}
	
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {


		ctx.channel().pipeline().fireChannelRead("Hello world -ChannelInboundHandle2-222");
	}
	
}
