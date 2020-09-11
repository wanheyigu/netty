package com.zxy.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class ChannelOutboundHandler3 extends ChannelOutboundHandlerAdapter{

	/*
	 * ChannelOutboundHandlerAdapter中的write方法可以接收信息，对信息进行处理。
	 *
	 * write(args...)方法如果要向下写数据，需要先接收到数据
	 */
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		
		System.out.println("333 - " + msg);

		/*
		 * 此方法会将两个参数传递给下一个结点的write(args...)方法，
		 * 注意：
		 *    ChannelOutboundHandlerAdapter查找的下一个结点是向前查找(按照配置顺序)
		 */   
		ctx.write(msg,promise);
	}
}
