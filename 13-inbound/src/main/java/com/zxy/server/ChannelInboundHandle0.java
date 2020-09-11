package com.zxy.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
/**
 * 自定义处理器继承ChannelInboundHandlerAdapter
 */
public class ChannelInboundHandle0 extends ChannelInboundHandlerAdapter{

	/*
	 * 实现向下传递功能
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

		System.out.println("ChannelInboundHandle0----->"+msg);
		
		/*
		 * 会触发下一个结点(处理器)的channelRead(args...)方法执行，
		 * 其会从当前结点开始向后查找下一个结点(根据加载处理器的顺序);
		 */
		ctx.fireChannelRead(msg);
		//super.channelRead(ctx, msg);
	}
	
	/*
	 * 在pipeline中可能会有多个处理器，且每个处理器都可能重写了channelActive(args...)方法，
	 * 但只有第一个处理器结点中的channelActive(args...)会执行，其他不会执行。
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {

		/*
		 * 会触发下一个结点(处理器)的channelRead(args...)方法执行，
		 * 与ctx.fireChannelRead(msg)不同的是：
		 * 其会从《head》结点开始向后查找下一个结点(根据加载处理器的顺序),
		 * 即，其会调用head结点的channelRead(args...)方法执行。
		 * 
		 * 注意：如果排在第一的处理器为实现channelActive(ChannelHandlerContext ctx)方法
		 * 那么会按照顺序向下找到第一个实现此方法的处理器，先执行。
		 */
		ctx.channel().pipeline().fireChannelRead("Hello world -ChannelInboundHandle0-000");
	}
	
}
