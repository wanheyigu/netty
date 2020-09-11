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
/** 拆包粘包
 * 需求：
 * 客户端发送数据
 */
public class SomeClient {

	public static void main(String[] args) throws InterruptedException {
		//创建客户端eventLoop
		NioEventLoopGroup group = new NioEventLoopGroup();
		
		try {
			//创建客户端bootStrap容器，装载channel和处理器
			Bootstrap bootstrap = new Bootstrap();
			//载入事件处理器
			bootstrap.group(group)
					 .channel(NioSocketChannel.class) //定义通信模型
					 .handler(new ChannelInitializer<SocketChannel>() { //装载业务处理器
						 //重写初始化channel类
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							//从channel中获取pipeline
							ChannelPipeline pipeline = ch.pipeline();
							//将业务处理器装载入pipeline
							/* 需求中客户端只需要发送数据，所有只用编码处理器就可以，关闭解码器
							 * pipeline.addLast("StringDecoder",new StringDecoder());
							 */
							
							/*
							 * 不使用StringEncoder，在发送消息的时候手动实现编码
							 */
							//pipeline.addLast("StringEncoder",new StringEncoder());
							
							//将自定义业务处理器装载入pipeline
							pipeline.addLast(new SomeClientHandler());
							
						}
						 
					 }); 
			//激活客户端channel
			ChannelFuture future = bootstrap.connect("localhost",8001).sync(); 
			//释放资源
			future.channel().closeFuture().sync();
		}finally {
			group.shutdownGracefully();
		}
	}
}
