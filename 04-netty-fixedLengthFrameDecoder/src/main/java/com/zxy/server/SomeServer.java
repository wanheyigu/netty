package com.zxy.server;

import com.zxy.server.SomeServerHandle;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class SomeServer {
	
	public static void main(String[] args) throws InterruptedException {
		//1 定义EventGroup,服务器端需要同时定parent处理客户端连接请求和child处理客户端业务请求
		NioEventLoopGroup parentGroup = new NioEventLoopGroup();
		NioEventLoopGroup childGroup = new NioEventLoopGroup();
		
		try {
			//定义bootstrap容器，装载通道与处理器
			ServerBootstrap bootstrap = new ServerBootstrap();
			//装载事件处理器、指定通信通道类型、装载业务处理器
			bootstrap.group(parentGroup, childGroup)
					 .channel(NioServerSocketChannel.class)
					 .childHandler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							// 初始化业务处理器，从channel中获取pipeline
							ChannelPipeline pipeline = ch.pipeline();
							
							/*
							 *  向pipeline中添加业务处理器
							 *  1 应用FixedLengthFrameDecoder解码器，固定长度帧解码器，即会按照指定的长度对Frame中的数据进行拆粘包；
							 *  2 DelimiterBasedFrameDecoder(5120,delimiter),第一个参数为Frame一帧的最大上限，第二个参数为指定的分隔符；
							 */
							int frameLength = 12;
							pipeline.addLast("FixedLengthFrameDecoder",new FixedLengthFrameDecoder(frameLength)); 
							
							pipeline.addLast("StringDecoder",new StringDecoder()); //字符串解码器
							
							/* 需求：服务器端只需要接收，因此关闭编码器
							 * pipeline.addLast("StringEncoder",new StringEncoder()); //字符串编码器
							 */
							
							//添加自定义业务处理器
							pipeline.addLast("SomeServerHandle", new SomeServerHandle());
							
						}
						 
					});
			//启动通信通道服务,同步操作
			ChannelFuture future = bootstrap.bind(8001).sync();
			System.out.println("服务器启动:8001");
			//释放资源清理通信通道(等待channel.close()执行时激活),同步
			future.channel().closeFuture().sync();		 
		}finally {
			//优雅关闭资源(资源空闲时关闭)
			parentGroup.shutdownGracefully();
			childGroup.shutdownGracefully();
		}
		
		
	}

}
