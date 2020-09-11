package com.zxy.servlet;

import java.io.UnsupportedEncodingException;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
/*
 * 创建响应对象
 */
public class CustomHttpResponse {
	
	private HttpRequest request;
	private ChannelHandlerContext context;
	
	public CustomHttpResponse(HttpRequest request, ChannelHandlerContext context) {
		super();
		this.request = request;
		this.context = context;
	}

	public void write(String content) throws Exception {
		/* 创建response对象：
		 * 应用长连接，使用HttpVersion.HTTP_1_1版本
		 * 对传入的字符串数据进行编码Unpooled.wrappedBuffer(content.getBytes("UTF-8"))
		 */
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
									HttpResponseStatus.OK,
									Unpooled.wrappedBuffer(content.getBytes("UTF-8")));
		//获取响应头
		HttpHeaders headers = response.headers();
		//初始化响应头
		//a.设置响应内容类型
		headers.set(HttpHeaderNames.CONTENT_TYPE, "text/json");
		//b.设置响应体长度
		headers.set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
		//设置缓存过期时长
		headers.set(HttpHeaderNames.EXPIRES, 0);
		//若http请求连接是长连接，则设置响应连接也为长连接
		if(HttpUtil.isKeepAlive(request)) {
			headers.set(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE);
		}
		//将响应对象写入channel
		context.writeAndFlush(response);
	}
	
}
