package com.zxy.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class SomeClient {
	public static void main(String[] args) throws InterruptedException, IOException {
		NioEventLoopGroup childgroup = new NioEventLoopGroup();
		
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(childgroup)
					 .channel(NioSocketChannel.class)
					 .handler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							
							ChannelPipeline pipeline = ch.pipeline();
							
							pipeline.addLast("LineBasedFrameDecoder",new LineBasedFrameDecoder(2048));
							
							pipeline.addLast("StringDecoder", new StringDecoder());
							pipeline.addLast("StringEncoder", new StringEncoder());
							
							pipeline.addLast("SomeClientHandler",new SomeClientHandler());
							
						}
					});
			ChannelFuture future = bootstrap.connect("localhost",8001).sync();
			System.out.println("客户端，已连接服务器");
			
			//获取键盘输入
			InputStreamReader is = new InputStreamReader(System.in,"UTF-8");
			//将输入内容封装进buffer
			BufferedReader br = new BufferedReader(is);
			//将输入的内容写入到channel,发送给服务器端
			future.channel().writeAndFlush(br.readLine()+"\r\n");
			
			//不关闭
			//future.channel().closeFuture().sync();
			
		}finally {
			//此处不要在关闭
			//childgroup.shutdownGracefully();
		}
	}

}
