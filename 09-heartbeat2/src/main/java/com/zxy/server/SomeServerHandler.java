package com.zxy.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class SomeServerHandler extends SimpleChannelInboundHandler<String> {
	
	//channel中一旦有数据过来就触发channelRead0方法
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		
		System.out.println(ctx.channel().remoteAddress()+"：接收到客户端发送的心跳----->"+msg);
		
	}
	
	//用户时间触发
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		//若发生了读空闲超时，则将连接断开
		if(evt instanceof IdleStateEvent) {
			/* 获取事件类型
			 *  READER_IDLE,WRITER_IDLE,ALL_IDLE
			 */
			IdleState state = ((IdleStateEvent)evt).state();
			//判断是否为读空闲超时事件
			if(state == IdleState.READER_IDLE) {
				System.out.println("读空闲超时，将连接断开");
				ctx.disconnect();//断开连接
				
			}else {
				//若发生其他事件，不做处理，继续运行
				super.userEventTriggered(ctx, evt);
			}
		}
	}
	
	
	//只要有异常产生就触发此方法
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
	
}
