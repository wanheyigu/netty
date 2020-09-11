package com.zxy.server;

import io.netty.channel.socket.SocketChannel;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
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
							/* 客户端应用了LengthFieldPrepender编码器进行编码，服务端要进行解码
							 * 参数：
							 * 1 maxFrameLength:一个帧的最大值，在不确定大小的时候一般填写Integer.MAX_VALUE;
    						   2 lengthFieldOffset:长度域偏移量;
    						   3 lengthFieldLength:长度域的长度,客户端定义为4,所有此处同样;
    						   4 lengthAdjustment:矫正值,在剔除长度域的值后,关注客户端传来的长度是否包含长度域的长度，如果包含就矫正，如不包含就为0;
       						   5 initialBytesToStrip:剔除值,剔除长度域的值;
							 */
							pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
							/*
							 * 服务器端接收到消息后要回复消息，加载编码器
							 */
							pipeline.addLast(new LengthFieldPrepender(4,true));
									
									
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
