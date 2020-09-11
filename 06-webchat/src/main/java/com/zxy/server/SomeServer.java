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
