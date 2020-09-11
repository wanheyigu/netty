package com.zxy.server;

import io.netty.channel.socket.SocketChannel;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

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
							/*
							 * 异常的传递是按照处理器装载到pipeline中的顺序，依次向后查找传递，不论Inbound还是Outbound。
							 */
							
							pipeline.addLast(new ChannelInboundHandle0());
							pipeline.addLast(new ChannelInboundHandle1());
							pipeline.addLast(new ChannelInboundHandle2());
							pipeline.addLast(new ChannelOutboundHandler1());
							pipeline.addLast(new ChannelOutboundHandler2());
							
							/*
							 * 异常处理器最后一个装载到pipeline
							 * 
							 * 这样装载前面的处理器无特殊需求就不用管异常处理了(同样需要捕获传递)。
							 */
							pipeline.addLast(new ExceptionHandler());
							
							
						}
					});
			ChannelFuture future = bootstrap.bind(8888).sync();
			System.out.println("服务器已启动！");
			future.channel().closeFuture().sync();
		}finally {
			parentGroup.shutdownGracefully();
			childGroup.shutdownGracefully();
			
		}
	}
}
