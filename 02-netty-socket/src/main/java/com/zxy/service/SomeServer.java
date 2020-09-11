package com.zxy.service;

import io.netty.channel.socket.SocketChannel;

import com.zxy.handle.SomeServerHandle;

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
							//StringDecoder：字符串解码器，将channel中的ByteBuf数据解码为String
							pipeline.addLast(new StringDecoder());
							//StringEncoder：字符串编码器，将String编码为将要发送到channel中的ByteBuf
							pipeline.addLast(new StringEncoder());
							pipeline.addLast("SomeServerHandle", new SomeServerHandle());
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
