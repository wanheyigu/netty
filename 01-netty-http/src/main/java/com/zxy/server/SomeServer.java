package com.zxy.server;

import com.zxy.init.SomeChannelInitializer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
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
					 .childHandler(new SomeChannelInitializer());  //指定childGroup处理器中的eventLoop所绑定的线程处理请求的处理器
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
