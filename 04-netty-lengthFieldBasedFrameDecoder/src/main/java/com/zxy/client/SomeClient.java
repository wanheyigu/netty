package com.zxy.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
//客户端启动类
public class SomeClient {
	
	public static void main(String[] args) throws InterruptedException {
		
		NioEventLoopGroup group = new NioEventLoopGroup();
		
		try {
			
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group)
					 .channel(NioSocketChannel.class)
					 .handler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							
							ChannelPipeline pipeline = ch.pipeline();
							/* 客户端先发消息，首先添加编码器
							 * pipeline.addLast(new LengthFieldPrepender(4,true));
							 * 如果客户端参数为LengthFieldPrepender(4,true)代表编码后的域长度包含长度域4的长度，服务端就要剔除，
							 * 如果客户端参数为LengthFieldPrepender(4)代表编码后的域长度不包含长度域4的长度，服务端就要剔除参数就为0，
							 */
							pipeline.addLast(new LengthFieldPrepender(4));
							
							/* 接收服务器端回复消息，添加解码器
							 * 参数：
							 * 1 maxFrameLength:一个帧的最大值，在不确定大小的时候一般填写Integer.MAX_VALUE;
    						   2 lengthFieldOffset:长度域偏移量;
    						   3 lengthFieldLength:长度域的长度,客户端定义为4,所有此处同样;
    						   4 lengthAdjustment:矫正值,在剔除长度域的值后,关注客户端传来的长度是否包含长度域的长度，如果包含就矫正，如不包含就为0;
       						   5 initialBytesToStrip:剔除值,剔除长度域的值;
							 */
							pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,-4,4));
							
							pipeline.addLast(new StringDecoder());
							pipeline.addLast(new StringEncoder());
							
							pipeline.addLast(new SomeClientHandler());
						}
					});
			ChannelFuture future = bootstrap.connect("localhost",8888).sync();
			future.channel().closeFuture().sync();
		}finally {
			group.shutdownGracefully();
		}
		
	}

}
