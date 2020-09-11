package com.zxy.client;

import java.rmi.activation.Activatable;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.unix.Buffer;
/*
 * 需求中客户端不需要读取数据，所有不需要重写channelRead0方法
 * 因此更换extends SimpleChannelInboundHandler<String>
 * 为extends ChannelInboundHandlerAdapter
 */
public class SomeClientHandler extends ChannelInboundHandlerAdapter{

	//定义发送数据内容
	private String massage = "Netty is a NIO client server framework " + 
			"which enables quick and easy development of network applications " 
			+ "such as protocol servers and clients. It greatly simplifies and " 
			+ "streamlines network programming such as TCP and UDP socket server." 
			+ "'Quick and easy' doesn't mean that a resulting application will " 
			+ "suffer from a maintainability or a performance issue. Netty has " 
			+ "this guide and play with Netty.In other words, Netty is an NIO " 
			+ "framework that enables quick and easy development of network " 
			+"as protocol servers and clients. It greatly simplifies and network " 
			+ "programming such as TCP and UDP socket server development.'Quick " 
			+ "not mean that a resulting application will suffer from a maintain" 
			+ "performance issue. Netty has been designed carefully with the expe " 
			+ "from the implementation of a lot of protocols such as FTP, SMTP, " 
			+ " binary and text-based legacy protocols. As a result, Netty has " 
			+ "a way to achieve of development, performance, stability, without " 
			+ "which enables quick and easy development of network applications " 
			+ "such as protocol servers and clients. It greatly simplifies and " 
			+ "streamlines network programming such as TCP and UDP socket server." 
			+ "'Quick and easy' doesn't mean that a resulting application will " 
			+ "suffer from a maintainability or a performance issue. Netty has " 
			+ "this guide and play with Netty.In other words, Netty is an NIO " 
			+ "framework that enables quick and easy development of network " 
			+ "as protocol servers and clients. It greatly simplifies and network " 
			+ "programming such as TCP and UDP socket server development.'Quick " 
			+ "not mean that a resulting application will suffer from a maintain" 
			+ "performance issue. Netty has been designed carefully with the expe " 
			+ "from the implementation of a lot of protocols such as FTP, SMTP, " 
			+ " binary and text-based legacy protocols. As a result, Netty has " 
			+ "a way to achieve of development, performance, stability, without " 
			+ "which enables quick and easy development of network applications " 
			+ "such as protocol servers and clients. It greatly simplifies and " 
			+ "streamlines network programming such as TCP and UDP socket server." 
			+ "'Quick and easy' doesn't mean that a resulting application will " 
			+ "suffer from a maintainability or a performance issue. Netty has " 
			+ "this guide and play with Netty.In other words, Netty is an NIO " 
			+ "framework that enables quick and easy development of network " 
			+ "as protocol servers and clients. It greatly simplifies and network " 
			+ "programming such as TCP and UDP socket server development.'Quick " 
			+ "not mean that a resulting application will suffer from a maintain" 
			+ "performance issue. Netty has been designed carefully with the expe " 
			+ "from the implementation of a lot of protocols such as FTP, SMTP, " 
			+ " binary and text-based legacy protocols. As a result, Netty has " 
			+ "a way to achieve of development, performance, stability, without " 
			+ "which enables quick and easy development of network applications " 
			+ "such as protocol servers and clients. It greatly simplifies and " 
			+ "framework that enables quick and easy development of network " 
			+ "as protocol servers and clients. It greatly simplifies and network " 
			+ "programming such as TCP and UDP socket server development.'Quick " 
			+ "not mean that a resulting application will suffer from a maintain" 
			+ "performance issue. Netty has been designed carefully with the expe " 
			+ "from the implementation of a lot of protocols such as FTP, SMTP, " 
			+ " binary and text-based legacy protocols. As a result, Netty has " 
			+"a way to achieve of development, performance, stability, without " 
			+ "which enables quick and easy development of network applications " 
			+ "such as protocol servers and clients. It greatly simplifies and " 
			+ "framework that enables quick and easy development of network " 
			+ "as protocol servers and clients. It greatly simplifies and network " 
			+ "programming such as TCP and UDP socket server development.'Quick " 
			+ "not mean that a resulting application will suffer from a maintain" 
			+ "performance issue. Netty has been designed carefully with the expe " 
			+ "from the implementation of a lot of protocols such as FTP, SMTP, " 
			+ " binary and text-based legacy protocols. As a result, Netty has " 
			+ "a way to achieve of development, performance, stability, without " 
			+ "which enables quick and easy development of network applications " 
			+ "such as protocol servers and clients. It greatly simplifies and " 
			+ "streamlines network programming such as TCP and UDP socket server." 
			+ "'Quick and easy' doesn't mean that a resulting application will " 
			+ "suffer from a maintainability or a performance issue. Netty has " 
			+ "this guide and play with Netty.In other words, Netty is an NIO " 
			+ "framework that enables quick and easy development of network " 
			+ "as protocol servers and clients. It greatly simplifies and network " 
			+ "programming such as TCP and UDP socket server development.'Quick " 
			+ "not mean that a resulting application will suffer from a maintain" 
			+ "performance issue. Netty has been designed carefully with the expe " 
			+ "from the implementation of a lot of protocols such as FTP, SMTP, " 
			+ " binary and text-based legacy protocols. As a result, Netty has " 
			+ "a way to achieve of development, performance, stability, without " 
			+ "a compromise.====================================================="
			+ System.getProperty("line.separator");//添加行分割符
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		/*
		 * 不使用StringEncoder，自己做编码工作;
		 * 其效果与使用StringEncoder效果一样
		 */
		//将massage转为二进制
		byte[] bmsg = massage.getBytes();
		//定义二进制缓存
		ByteBuf buffer = null;
		//循环发送消息
		for(int i=0;i<2;i++) {
			//申请缓存
			buffer = Unpooled.buffer(bmsg.length);
			//将数据写入缓存
			buffer.writeBytes(bmsg);
			//发送消息
			ctx.writeAndFlush(buffer);
		}
		
//		//客户端发送两次大消息
//		ctx.writeAndFlush(massage);
//		ctx.writeAndFlush(massage);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		
		ctx.close();
		
	}

}
