package com.zxy.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class SomeServer {

	public static void main(String[] args) throws InterruptedException {
		
		//创建EventLoopGroup
		NioEventLoopGroup parentGroup= new NioEventLoopGroup();
		NioEventLoopGroup childGroup = new NioEventLoopGroup();
		
		try {
			//创建bootstrap
			ServerBootstrap bootstrap = new ServerBootstrap();
			
			//装载处理器
			bootstrap.group(parentGroup,childGroup)
					 .channel(NioServerSocketChannel.class)
					 .childHandler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel ch) throws Exception {

							//获取pipeline
							ChannelPipeline pipeline = ch.pipeline();
							
							pipeline.addLast("StringDecoder",new StringDecoder());
							pipeline.addLast("StringEncoder",new StringEncoder());
							
							pipeline.addLast("",new SomeServerHandler());
						}
					});
			ChannelFuture future = bootstrap.bind(8001).sync();
			System.out.println("服务器启动：8001");
			future.channel().closeFuture().sync();
		}finally {
			parentGroup.shutdownGracefully();
			childGroup.shutdownGracefully();
		}
	}
}
