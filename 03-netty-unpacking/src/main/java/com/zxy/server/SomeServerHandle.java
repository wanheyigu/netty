package com.zxy.server;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
/* 需求：服务端接收客户端发送来的大数据包
 * 因此自定义处理器应用实现SimpleChannelInboundHandler<String>，因为此处理器有自动释放msg消息的功能；
 * 而ChannelInboundHandlerAdapter处理器不会将消息自动释放。
 * 
 */
public class SomeServerHandle extends SimpleChannelInboundHandler<String> {
	//计数器
	private int counter;
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

		System.out.println("server端接收到的第["+ ++ counter +"]个数据包:"+msg);
		
	}
	
	/*
	 * @Override public void channelRead(ChannelHandlerContext ctx, Object msg)
	 * throws Exception { //将来自与客户端的数据显示在服务端控制台 System.out.println(msg); //向客户端发送数据
	 * ctx.channel().writeAndFlush("from Server:"+UUID.randomUUID()); //休眠1秒钟
	 * TimeUnit.MILLISECONDS.sleep(1000); }
	 */
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		
		ctx.close();
	}

	

}
