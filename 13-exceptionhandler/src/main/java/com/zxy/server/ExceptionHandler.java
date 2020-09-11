package com.zxy.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
/**
 * 自定义异常处理器：
 *    工程中约定会定义一个异常处理器来处理异常，异常处理器捕获异常后不在向下传递，主动处理；
 * 注：
 *    1.根据业务需求不同其他处理器也可以捕获，同样按照业务要求来处理或不处理；
 *    2.异常处理器必须最后一个装载到pipeline；
 */
public class ExceptionHandler extends ChannelInboundHandlerAdapter{

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		/*
		 * 判断异常类型来决定如何处理
		 */
		if(cause instanceof Exception) {
			System.out.println("发送Exception！");
			//处理...
		}
		if(cause instanceof ArrayIndexOutOfBoundsException) {
			System.out.println("发送ArrayIndexOutOfBoundsException");
			//处理...
		}
		
	}
	
}
