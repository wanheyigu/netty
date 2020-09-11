package com.zxy.tomcat;

import com.zxy.service.SomeServlet;
import com.zxy.servlet.CustomHttpRequest;
import com.zxy.servlet.CustomHttpResponse;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
/**
 * 自定义处理器
 * 继承ChannelInboundHandlerAdapter是因为需求需要将请求的数据响应回客户端，
 * ChannelInboundHandlerAdapter不会自动释放接收到的数据。
 * SimpleChannelInbounHandler会自动释放接收到的数据。
 */

public class TomcatSeverHandler extends ChannelInboundHandlerAdapter{
	/* 此处需求决定继承对象
	 * 1.若需要将msg返回给客户端，则需要继承ChannelInboundHandlerAdapter
	 * 2.否则，如不需要返回则继承SimpleChannelInbounHandler
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		//判断msg是否为HttpRequest请求
		if(msg instanceof HttpRequest) {
			HttpRequest request = (HttpRequest) msg;
			
			//如果是即执行业务,即调用servlet
			SomeServlet servlet = new SomeServlet();
			/* 调用doGet()方法处理请求：
			 * doGet()方法中调用了response.write(content);
			 * write(content)方法中调用了context.writeAndFlush(response);
			 * 所有此处无需在writeAndFlush;
			 */
			servlet.doGet(new CustomHttpRequest(request, ctx), new CustomHttpResponse(request, ctx));
			
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		cause.printStackTrace();
		ctx.close();
	}
}
