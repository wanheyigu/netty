package com.zxy.tomcat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;


/* tomcat服务端*/
public class TomcatServer {

	public static void main(String[] args) throws Exception {
		/* 按照netty的服务端创建原则，需要有两个group，一个处理连接，一个处理请求 
		 * 1.NioEventLoopGroup()创建默认会在其内部创建应用服务器实际逻辑内核*2的EventLoop
		 */
		EventLoopGroup parentGroup = new NioEventLoopGroup();
		EventLoopGroup childGroup = new NioEventLoopGroup();
		
		try {
			/* 服务端应用ServerBootstrap
			 * bootstrap的作用即为粘合剂，将多数组件装载其中，然后由其启动(即启动类)
			 */
			ServerBootstrap bootstrap = new ServerBootstrap();
			
			bootstrap.group(parentGroup,childGroup)
					 /* 指定用于存放连接请求队列的长度
					  * 例，处理连接请求的parentGroup默认一次处理16个连接请求，但现有连接请求
					  * 数为160个，并且其中一部分已经完成三次握手，那么就会将等待处理的连接请求存放
					  * 在一个临时队列中，此队列默认长度为50个，此处指定长度为1024；
					  * SO_BACKLOG不是netty的参数，其是Socket的标准参数
					  */
					 .option(ChannelOption.SO_BACKLOG, 1024)
					 /* 指定使用心跳机制来维护长连接
					  * 客户端与服务端的连接方式为长连接(tcp长连接)，在tcp协议中规定，长连接2个小时
					  * 没有任何通信即关闭，所有通过心跳机制来维护；
					  */
					 .childOption(ChannelOption.SO_KEEPALIVE, true)
					 .channel(NioServerSocketChannel.class)
					 .childHandler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							// 获取ChannelPipline
							ChannelPipeline pipeline = ch.pipeline();
							/* 添加编解码器:
							 * 客户端发送的请求进入传输管道后会转为BetyBuffer二进制，服务端需要通过解码来获取，
							 * 服务端响应的数据需要通过编码为BetyBuffer二进制后才能通过管道传输给客户端，
							 * 所以需要添加编辑码器；
							 * HttpRequestDecoder和HttpResponseEncoder无顺序要求，因其完成工作不同
							 */
							//pipeline.addLast("HttpRequestDecoder",new HttpRequestDecoder());
							//pipeline.addLast("HttpResponseEncoder",new HttpResponseEncoder());
							//HttpServerCodec是HttpRequestDecoder和HttpResponseEncoder的结合体
							pipeline.addLast("HttpServerCodec",new HttpServerCodec());
							
							//添加自定义
							pipeline.addLast("TomcatSeverHandler",new TomcatSeverHandler());
						}});
			//绑定8001端口，并且同步启动服务
			ChannelFuture future = bootstrap.bind(8001).sync();
			System.out.println("Tomcat已启动,端口号:8001");
			
			future.channel().closeFuture().sync();
		}finally {
			parentGroup.shutdownGracefully();
			childGroup.shutdownGracefully();
		}
	}

}
