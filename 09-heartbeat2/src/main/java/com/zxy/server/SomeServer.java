package com.zxy.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
/*
 * 服务端
 */
public class SomeServer {

	public static void main(String[] args) throws Exception {
		/* 创建两个EventLoopGroup:
		 * parentGroup用于处理连接请求
		 * childGroup用于处理实际业务请求
		 */
		EventLoopGroup parentGroup = new NioEventLoopGroup();
		EventLoopGroup childGroup = new NioEventLoopGroup();
		
		try {
			//创建bootstrap粘合剂
			ServerBootstrap bootstrap = new ServerBootstrap();
			//将两个EventLoopGroup装载到bootstrap中
			bootstrap.group(parentGroup, childGroup)
					 .channel(NioServerSocketChannel.class) //确认通道
					 .childHandler(new ChannelInitializer<SocketChannel>(){ //装载业务处理器

						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							//获取pipeline通信线
							ChannelPipeline pipeline = ch.pipeline();
							pipeline.addLast("StringDecoder", new StringDecoder());
							//pipeline.addLast("StringEncoder", new StringEncoder());
							//装载空闲检查处理器，根据业务设置(5,0,0)，5秒钟内没有读操作即触发事件(触发事件在自定义处理器中实现)
							pipeline.addLast("IdleStateHandler",new IdleStateHandler(5,0,0));
							pipeline.addLast("SomeServerHandler",new SomeServerHandler());
						}
						 
					 });
			ChannelFuture future = bootstrap.bind(8001).sync();
			System.out.println("Server is start on the 8001");
			
			future.channel().closeFuture().sync();
			
		}finally {
			parentGroup.shutdownGracefully();
			childGroup.shutdownGracefully();
		}
	}
}
