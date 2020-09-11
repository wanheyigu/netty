package com.zxy.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class SomeClient {
	public static void main(String[] args) throws Exception {
		NioEventLoopGroup group = new NioEventLoopGroup();
		
		//try {
			Bootstrap bootstrap = new Bootstrap();
			
			bootstrap.group(group)
					 .channel(NioSocketChannel.class)
					 .handler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel ch) throws Exception {

							ChannelPipeline pipeline = ch.pipeline();
							//pipeline.addLast("StringDecoder", new StringDecoder());
							pipeline.addLast("StringEncoder", new StringEncoder());
							
							pipeline.addLast("SomeClientHandler",new SomeClientHandler());
						}
					});
			
			bootstrap.connect("localhost",8001).sync();
			/*
			 * 心跳业务中，客户端与服务端关闭连接的权利在服务端，所以此处不需要future对象及相关关闭操作；
			 * 如客户端发现服务端关闭连接后，也要关闭本地连接，那么需要在自定义handler中有条件操作。
			 */
			//ChannelFuture future = bootstrap.connect("localhost",8001).sync();
			
			//future.channel().closeFuture().sync();
			
		//}finally {
			//group.shutdownGracefully();
		//}
	}

}
