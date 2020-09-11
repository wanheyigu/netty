package com.zxy.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
/**
 * 自定义服务端处理器：需要继承自ChannelInboundHandlerAdapter
 * 需求：<核心业务处理在此>
 * 用户提交一个请求后，在浏览器上就会看到hello netty world！
 */
public class SomeServerHandler extends ChannelInboundHandlerAdapter{

	/**
	 * 重写channelRead方法，此方法就是回调方法；
	 * 当channel中有来自client客户端的数据时就会触发该方法的执行；
	 * @param ctx 上下文对象
	 * @param msg 就是来自于client中的数据
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		//super.channelRead(ctx, msg);
		//获取msg内容
		//System.out.println("msg"+msg.getClass());
		//获取管道，通过管道获取client地址
		//System.out.println("客户端地址"+ctx.channel().remoteAddress());
		
		/* 业务处理：
		 * 1 判断msg是否来自于HttpRequest(正常情况下来自于io.netty.handler.codec.http.DefaultHttpRequest)
		 * 2如果符合来源，获取msg
		 */
		
		if(msg instanceof HttpRequest) {
			//强制转为HttpRequest
			HttpRequest request = (HttpRequest) msg;
			System.out.println("请求方式："+request.method().name());
			System.out.println("请求URI："+request.getUri());
			
			if("/favicon.ico".equals(request.uri())) {
				System.out.println("不处理/favicon.ico请求。");
				return;
			}
			
		
			//构造response响应体(内容)
			ByteBuf body = Unpooled.copiedBuffer("hello netty world！", CharsetUtil.UTF_8);
			//生产响应对象
			DefaultFullHttpResponse response =  new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,body );
			//获取response响应头head进行初始化
			HttpHeaders headers = response.headers();
			headers.set(HttpHeaderNames.CONTENT_TYPE,"text/plain");
			headers.set(HttpHeaderNames.CONTENT_LENGTH,body.readableBytes());
		
			//将响应对象写入到channel
			//ctx.write(response);
			//ctx.flush();
			//常用写法
			ctx.writeAndFlush(response)
				//添加监听器(一般情况下不会如此做)，响应体发送完毕则直接将channel关闭<触发SomeServer中future.channel().closeFuture().sync();>
				.addListener(ChannelFutureListener.CLOSE);
		
		}
		
	}
	
	/**
	 * 当channel中的数据在处理过程中出现异常时会触发该方法的执行
	 * @param ctx 上下文
	 * @param cause 发生的异常对象
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		//super.exceptionCaught(ctx, cause);
		//处理过程中出现异常会触发
		cause.printStackTrace();
		
		/* 关闭Channel：
		 * 此处关闭管道后会触发SomeServer中future.channel().closeFuture().sync(); 
		 */
		ctx.close();
	}
}
