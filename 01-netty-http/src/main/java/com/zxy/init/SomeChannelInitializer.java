package com.zxy.init;

import com.zxy.handler.SomeServerHandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * channel管道初始化器
 * 一般不会这样写：
 * 当前类的实例在pipeline初始化完毕后就会被GC，所有创建其实例没有意义
 * 实际使用在SomeServer中通过创建匿名内部类的方式；
 */
public class SomeChannelInitializer extends ChannelInitializer<SocketChannel>{

	/*
	 * 当Channel初始创建完毕后就会触发该方法执行，
	 * 用于初始化Channel;
	 */
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

}
