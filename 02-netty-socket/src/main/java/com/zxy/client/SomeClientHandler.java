package com.zxy.client;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 自定义客户端处理器
 * 需要继承SimpleChannelInboundHandler<String>
 * <String>泛型即消息类型
 */
public class SomeClientHandler extends SimpleChannelInboundHandler<String>{

	//msg的消息类型与类中的泛型是一致的
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		
		System.out.println(ctx.channel().remoteAddress()+"----->"+msg);
		ctx.channel().writeAndFlush("from client:"+LocalDateTime.now());
		TimeUnit.MILLISECONDS.sleep(500);
	}
	
	/**客户端主动发送消息给服务端：
	 * 如果不重写此方法，那么服务端与客户端就形成了死锁，即服务端等待客服端发送消息然后回复，
	 * 客户端同样在等待服务端发送消息然后回复，因此需要有一方主动发送消息，才能实现消息的传输；
	 * channelActive(ChannelHandlerContext ctx)方法是当Channel管道被激活后
	 * 会触发该方法执行，即客户端启动类中的channel一旦被创建初始化后，此方法就执行，所以通过
	 * 此方法来发送首个通信消息。
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {

		ctx.channel().writeAndFlush("first msg from client"+LocalDateTime.now());
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		
		ctx.close();
	}

}
