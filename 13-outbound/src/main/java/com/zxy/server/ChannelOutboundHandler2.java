package com.zxy.server;

import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class ChannelOutboundHandler2 extends ChannelOutboundHandlerAdapter{

	/* ChannelOutboundHandler中的方法都是异步执行。
	 * ChannelOutboundHandlerAdapter中的write方法可以接收信息，对信息进行处理。
	 * 
	 * write(args...)方法如果要向下写数据，需要先接收到数据
	 */
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		
		System.out.println("222 - " + msg);

		/*
		 * 此方法会将两个参数传递给下一个结点的write(args...)方法，
		 * 注意：
		 *    ChannelOutboundHandlerAdapter查找的下一个结点是向前查找(按照配置顺序)
		 */   
		ctx.write(msg,promise);
	}
	
	/*
	 * ChannelOutboundHandlerAdapter可以应用handlerAdded(args...)方法
	 * 触发ChannelOutboundHandlerAdapter的write(args...)方法。
	 */
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		
		/*
		 * 其会将消息传递给下一个结点的write(args...)方法，
		 * 注意：
		 *    此处的下一个结点，是从tail结点开始查找的，
		 *    即其会将消息传递给tail结点的write(args...)方法。
		 *    
		 * 需要添加定时任务执行，否则输出结果将没有最后一个自定义处理器内容；
		 *	  因为，此处的write(args...)的执行与向pipeline中添加处理器是
		 *	  异步执行的，所有write(args...)只管向下写向下传；
		 *   流程：一旦ChannelOutboundHandler2处理器添加完成即触发handlerAdded(args...)
		 *       方法执行，不加定时任务的时候，触发就向下传，直接传给了tail，tail在查找下一个，
		 *       此时ChannelOutboundHandler3还没有添加进pipeline，所有tail向前查找的
		 *       下一个是ChannelOutboundHandler2，因此ChannelOutboundHandler2的write(agrs...)
		 *       执行了，然后在向前的下一就执行了ChannelOutboundHandler1的write(agrs...)；
		 * 
		 * 因此添加定时任务延迟执行。
		 */
		//ctx.channel().write("Hello world 222");
		
		ctx.executor().schedule(() -> {
			ctx.channel().write("Hello world 222");
		},1,TimeUnit.SECONDS);
		
		
		
	}
}
