package com.zxy.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

public class SomeServer {

	public static void main(String[] args) throws InterruptedException {

		NioEventLoopGroup parentGroup = new NioEventLoopGroup();
		NioEventLoopGroup childGroup = new NioEventLoopGroup();
		
		try {
			ServerBootstrap bootBootstrap = new ServerBootstrap();
			bootBootstrap.group(parentGroup, childGroup)
						  .channel(NioServerSocketChannel.class)
						  .childHandler(new ChannelInitializer<SocketChannel>() {

							@Override
							protected void initChannel(SocketChannel ch) throws Exception {
								
								ChannelPipeline pipeline = ch.pipeline();
								
								/*以下添加的处理器顺序不可以颠倒*/
								//添加http编解码器
								pipeline.addLast("HttpServerCodec",new HttpServerCodec());
								//添加大块数据chunk处理器
								pipeline.addLast(new ChunkedWriteHandler());
								//添加chunk聚合处理器
								pipeline.addLast(new HttpObjectAggregator(4096));
								//添加WebSocket转换处理器
								pipeline.addLast("StringEncoder",new WebSocketServerProtocolHandler("/some"));
								//自定义业务处理器
								pipeline.addLast("SomeServerHandler",new SomeServerHandler());
							}
						});
			ChannelFuture future = bootBootstrap.bind(8001).sync();
			System.out.println("服务器启动成功，监听端口号为：8001；");
			future.channel().closeFuture().sync();
		}finally {
			parentGroup.shutdownGracefully();
			childGroup.shutdownGracefully();
		}

	}

}
