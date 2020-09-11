package com.zxy.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

public class SomeServer {

	public static void main(String[] args) throws InterruptedException {
		
		NioEventLoopGroup parentGroup = new NioEventLoopGroup();
		NioEventLoopGroup childGroup = new NioEventLoopGroup();
		
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(parentGroup, childGroup)
					 .channel(NioServerSocketChannel.class)
					 .childHandler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel ch) throws Exception {

							ChannelPipeline pipeline = ch.pipeline();
							
							//聊天程序业务基本是基于行的信息，所以此处添加LineBasedFrameDecoder行解码器
							pipeline.addLast("LineBasedFrameDecoder", new LineBasedFrameDecoder(2048));
							/*
							 * 添加空闲检查处理器:参数(readerIdleTimeSeconds,writerIdleTimeseconds,allIdleTimeSeconds)
							 * 1 readerIdleTimeSeconds设置读空闲时限，设置为0即不触发
							 * 2 writerIdleTimeseconds设置写空闲时限，设置为0即不触发
							 * 3 allIdleTimeseconds设置读写空闲时限，设置为0即不触发
							 * allIdleTimeseconds官方：规定时限内读写操作发生一个都不触发关闭；
							 * 实际应用是其必须要在规定时限内即发生读又发生写才会不触发。
							 * IdleStateHandler(3,5,0):若3秒内当前服务器没有发生读操作，则会触发读操作空闲事件；
							 * 						        若5秒内当前服务器没有发生写操作，则会触发写操作空闲事件；
							 * 						        不启用allIdleTimeseconds；
							 */
							pipeline.addLast("IdleStateHandler",new IdleStateHandler(3,5,0));
							/*
							 * pipeline.addLast("IdleStateHandler",new IdleStateHandler(0,0,5));
							 * 必须在5秒内读写操作都发生才不触发。
							 */
							
							
							pipeline.addLast("StringDecoder", new StringDecoder());
							pipeline.addLast("StringEncoder", new StringEncoder());	
							
							pipeline.addLast("SomeServerHandler",new SomeServerHandler());
						}
					});
			ChannelFuture future = bootstrap.bind(8001).sync();
			System.out.println("服务器已启动：8001");
			future.channel().closeFuture().sync();
		}finally {
			parentGroup.shutdownGracefully();
			childGroup.shutdownGracefully();
		}

	}

}
