package com.zxy.server;

import com.zxy.handler.SomeServerHandler;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
/**
 * 服务器启动类
 * 
 *
 */
public class SomeServer {

	public static void main(String[] args) {
		
			// 用于处理客户端连接请求，连接成功后将请求发送给childGroup中的eventloop处理
			EventLoopGroup parentGroup =  new NioEventLoopGroup();
			// 用于处理客户端请求
			EventLoopGroup childGroup =  new NioEventLoopGroup();
			try {
			//用于启动ServerChannel
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(parentGroup,childGroup) //指定eventLoopGroup
			         .channel(NioServerSocketChannel.class) //指定使用NIO进行通信
					 .childHandler(new ChannelInitializer<SocketChannel>() { //创建匿名内部类来初始化处理器

						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							/*	将channel对象放入pipeline中；
							 *  从Channel中获取pipeline；(可以直接获取是因为在创建channel的时候已经创建了pipeline)
							 */
							ChannelPipeline pipeline = ch.pipeline();
							/*
							 * 常用的add方法是addLast，按照程序逻辑加入到最后；
							 * 常用的addLast有两个，单参数和双参数;
							 * 将HttpServerCodec处理器放入到pipeline的最后；
							 * 
							 * HttpServerCodec处理器(继承自ChannelHandle)：可以非常容易的解决server端Http请求的编解码；
							 * 是HttpRequestDecoder解码器与HttpResponseEncoder编码器的结合体，
							 * 此处使用它来完成http传输中的编解码工作；
							 * <client发送request请求进入channel管道转变为bytebuffer二进制，
							 * server需要将二进制请求转换为HttpRequest请求才能处理，所有需要通过
							 * 解码(HttpServerCodec->HttpRequestDecoder)，处理完成后发送
							 * 响应进入channel管道，需要将响应HttpResponse转变为二进制，所有需要
							 * 通过编码(HttpServerCodec->HttpResponseEncoder);>
							 * HttpRequestDecoder：Http请求解码器:将channel中的ByteBuffer数据解码为HttpRequest对象；
							 * HttpResponseEncoder：Http响应编码器:将HttpResponse对象编码为可以在channel中发送的ByteBuffer数据；
							 */
							pipeline.addLast(new HttpServerCodec());
							//pipeline.addLast("HttpServerCodec", new HttpServerCodec());
							
							/*
							 * 将自定义的处理器放入到pipeline的最后，这样上一个就排在了此处理器前；
							 * 这就是常使用addLast的方式；
							 */
							pipeline.addLast("defined", new SomeServerHandler());
						}
					});  //指定childGroup处理器中的eventLoop所绑定的线程处理请求的处理器
			
			/* 指定当前服务器所监听的端口号
			 * bind()方法的执行时异步的，程序会继续向下执行，造成未绑定即关闭错误
			 * 所以使用.sync()将此步操作变为同步
			 * .sync()方法会使bind()操作与后续的代码的执行由异步变为同步
			 */
			ChannelFuture future = bootstrap.bind(8888).sync();
			System.out.println("服务器启动成功，监听端口号为：8888；");
			/*
			 * 关闭channel，使用.sync()将此步操作变为同步
			 * 当channel调用了close()方法并关闭成功后才会触发closeFuture()方法执行
			 * closeFuture()方法的本质是做收尾工作，将一些资源释放掉
			 */
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			// TODO: handle exception
		}finally {
			/*
			 * 优雅关闭：未执行完成不关闭
			 */
			parentGroup.shutdownGracefully();
			childGroup.shutdownGracefully();
		}
		
	}
}
