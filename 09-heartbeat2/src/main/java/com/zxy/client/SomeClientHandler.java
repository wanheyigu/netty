package com.zxy.client;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.ScheduledFuture;
/*
 * 业务场景中不需要读数据，所以继承ChannelInboundHandlerAdapter
 * 
 * 增加功能：当客户端发现连接断开后，马上重连
 */
public class SomeClientHandler extends ChannelInboundHandlerAdapter{
	
	
	
	private ScheduledFuture<?> schedule;
	private GenericFutureListener listener;

	private Bootstrap bootstrap;
	
	public SomeClientHandler(Bootstrap bootstrap) {
		super();
		this.bootstrap = bootstrap;
	}
	//当Channel被激活后(即客户端连接服务端)触发该方法执行(该方法连接成功后只执行一次)
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		//ctx.channel().writeAndFlush("~PING~");
		//随机发生心跳
		randomSendHeartBeat(ctx.channel());
	}
	
	//当Channel被钝化(关闭)就会触发该方法执行，执行一次发现未激活还会继续执行，直到激活(执行成功)
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		//通道关闭先移除递归发送监听
		schedule.removeListener(listener);
		
		
		/*
		 * 使用定时任务,没一秒钟重连一次
		 * 此处场景业务逻辑为只要连接未成功，那么netty会重复调用channelInactive()方法
		 * 执行以下的操作。
		 */
		ctx.channel().eventLoop().schedule(()->{
			try {
				//10秒重连
				Thread.sleep(10000);
				// 重新连接服务端
				System.out.println("Reconnecting......");
				bootstrap.connect("localhost",8001).sync();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, 1, TimeUnit.SECONDS);
	}
	
	
	
	
	private void randomSendHeartBeat(Channel channel) {
		/* 1.为演示业务逻辑，此处随机生成1至8秒发送心跳间隔时间；
		 * 2.此处需要通过定时任务来调用发送；
		 */
		//生成一个1至8的随机数，作为心跳发送间隔；
		int hearBeatInternal = new Random().nextInt(7)+1;
		System.out.println(hearBeatInternal+"秒后将发送下一次心跳");
		/* 使用schedule定时任务触发定时发送
		 * 1.command：Runnable接口，用于执行发送任务，无输入无返回值，使用lambda表达式
		 * 2.delay：long,间隔时间
		 * 3.unit：TimeUnit,时间单位
		 */
		schedule = channel.eventLoop().schedule(() -> {
			//此处编写到达hearBeatInternal时间要执行的内容
			//判断通道是否存活
			if(channel.isActive()) {
				//若channel存活则向服务器发送心跳
				System.out.println("向Server发送心跳");
				channel.writeAndFlush("~PING~");
			}else {
				//若channel已关闭，则关闭连接
				System.out.println("与Server的连接已断开");
				/* 该语句的执行会触发channelInactive()方法的执行；
				 * 由于randomSendHeartBeat()方法在递归调用，所以该语句会被一直执行，
				 * 也会触发channelInactive()一直执行。
				 */
				//channel.closeFuture();
				
			}
			
		}, hearBeatInternal, TimeUnit.SECONDS);
		
		//为异步定时任务添加监听器，来实现递归调用，循环向服务端发送心跳
		listener = (future)->{
			/* 
		     * 若异步定时任务执行成功，则重新再次随机发送心跳
		     * 递归调用
		     */
			randomSendHeartBeat(channel);
		};
		schedule.addListener(listener);
	}
	
	
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}
