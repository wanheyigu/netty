package com.zxy.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * 需求：聊天群服务端程序
 * 接收任意客户端发送的消息，并且将消息发送给其他客户端，即接收又发送
 * 因此继承ChannelInboundHandlerAdapter处理器，不释放消息
 */
public class SomeServerHandler extends ChannelInboundHandlerAdapter{

	/* 需要创建一个ChannelGroup，来管理所以与服务端连接的客户端
	 * ChannelGroup是一个线程安全的集合，其中存放与当前服务器相连接的所有Active状态的Channel，
	 * 提供针对这些channel的批量操作方法;
	 * GlobalEventExecutor是一个单例、单线程的EventExecutor：是为了
	 * 保证整个ChannelGroup中的处理操作都是由一个线程来完成；
	 */
	private static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	
	//只要有客户端channel给当前的服务端发送消息，那么就会触发channelRead()方法
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		//获取当前发送消息的客户端channel
		Channel channel = ctx.channel();
		
		/* 此处实现将消息广播给ChannelGroup中所有的客户端channel
		 * 1 消息包含发送者地址channel.remoteAddress()
		 * 2发送者自己收到消息不显示地址，显示为自己发送，即实现发送给自己的消息与发送给他人的消息不一致
		 * 遍历group中的channel
		 */
		group.forEach(ch -> {
			//当前发送的不是自己
			if(ch != channel) {
				/*
				 * 此处发送是ch不是自己，那么就是给除了自己以为的ch发送消息
				 * group.writeAndFlush(channel.remoteAddress()+":"+msg+"\n");
				 */
				ch.writeAndFlush(channel.remoteAddress()+":"+msg+"\n");
			}else{
				/*
				 * 如果是自己，处理消息给自己发
				 * group.writeAndFlush("me:"+msg+"\n");
				 */
				channel.writeAndFlush("me:"+msg+"\n");
			}
		});
	}
	
	/* 一旦有客户端与服务器连接成功就会执行此方法
	 * 应用此方法来添加channel到ChannelGroup
	 * (即channel一旦被激活就添加到ChannelGroup中)
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		//获取到当前与服务器连接成功的channel
		Channel channel = ctx.channel();
		System.out.println(channel.remoteAddress()+"----->上线");
		group.writeAndFlush(channel.remoteAddress()+"----->上线\n");
		/*
		 * 将当前channel添加到group中；
		 * group.add(channel)就放在此位置，不放置在前面，
		 * 前面发送的消息是通知其他客户端当前客户端上线，此消息不需要包含自己
		 */
		group.add(channel);
	}
	
	/* 一旦有客户端channel与服务器端断开连接就会执行此方法
	 * 应用此方法将channel从ChannelGroup中移除
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		//获取到当前与服务器断开连接的channel
		Channel channel = ctx.channel();
		System.out.println(channel.remoteAddress()+"---------->下线");
		group.writeAndFlush(channel.remoteAddress()+"下线：当前在线人数为："+group.size()+"\n");
		
		/**
		 * ChannelGroup中存放的都是Active的channel，一旦某channel的状态不再试Active，
		 * ChannelGroup会自动将其从集合中剔除，所以下面的语句不用写。
		 * group.remove(channel);
		 * remove()方法的场景是将一个Active状态的channel移除ChannelGroup时使用。
		 */
	}
	
	/** 
	 * 所有规定动作以外的所有事件都可以通过userEventTriggered()方法触发
	 * 使用此方法实现空闲检测触发事件
	 */
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		//判断evt类型，决定采用对应操作(赛选事件)
		if(evt instanceof IdleStateEvent) {
			IdleStateEvent event = (IdleStateEvent) evt;
			String eventDes = null;
			switch (event.state()) {
			case READER_IDLE:eventDes = "读空闲超时";break;
			case WRITER_IDLE:eventDes = "写空闲超时";break;
			//case ALL_IDLE:eventDes = "读写空闲超时";
			//就三种状态，因此default就相当于 case ALL_IDLE:eventDes = "读写空闲超时";
			default:eventDes = "读写空闲超时";
				break;
			}
			System.out.println(eventDes);
			//关闭超时连接
			ctx.close();
		}else {//如未匹配到需要处理的事件，正常执行
			super.userEventTriggered(ctx, evt);
		}
	}
	
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

		cause.printStackTrace();
		
		ctx.close();
	}
}
