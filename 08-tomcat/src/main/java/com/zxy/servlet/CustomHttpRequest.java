package com.zxy.servlet;

import java.util.List;
import java.util.Map;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
/**
 * 定义servlet请求方式
 * 即request方式
 *
 */
public class CustomHttpRequest {
	
	private HttpRequest request;
	private ChannelHandlerContext context;
	
	public CustomHttpRequest(HttpRequest request, ChannelHandlerContext context) {
		super();
		this.request = request;
		this.context = context;
	}
	//获取请求地址，其中包含请求路径与请求参数
	public String getUri() {
		return request.getUri();
	}
	//获取请求方式(post、get)
	public String getMethod() {
		return request.method().name();
	}
	//获取请求参数列表
	public Map<String,List<String>> getParameters(){
		/*
		 * 通过QueryStringDecoder解码器来解析uri，从uri中获取请求参数
		 */
		QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
		
		return decoder.parameters();
	}
	/* 获取指定名称的参数
	 * 通过调用获取参数列表方法，获取参数列表，然后向参数列表传入指定名称获取名称对应的参数值
	 */
	public List<String> getParameters(String name){
		return this.getParameters().get(name);
	}
	
	public String getParameter(String name) {
		//防止空值异常
		List<String> list = this.getParameters(name);
		if(list == null) {
			return null;
		}
		return list.get(0);
	}
	//获取请求路径
	public String getPath() {
		
		return new QueryStringDecoder(request.uri()).path();
	}

}
