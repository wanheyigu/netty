package com.zxy.server;

import java.util.UUID;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
/**
 * 需求：接收客户端左侧文本域发送过来的消息，并将消息返回给客户端
 * 因此需要继承ChannelInboundHandlerAdapter，
 * 此处理器可以缓存数据，不会自动释放掉。
 * 
 *
 */
public class SomeServerHandler extends ChannelInboundHandlerAdapter{
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		//将msg转换为String 类型
		String text = ((TextWebSocketFrame)msg).text();
		//处理一下数据【转换为String就是为了模拟处理，在msg前添加一个字符串“Frome Client:”，如不需处理，直接发送回去即可】
		ctx.channel().writeAndFlush(new TextWebSocketFrame("Frome Client:"+text));
		
		/*
		 * if(msg instanceof HttpRequest) { HttpRequest request = (HttpRequest) msg;
		 * 
		 * }
		 */
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

		cause.printStackTrace();
		ctx.close();
	}

}
