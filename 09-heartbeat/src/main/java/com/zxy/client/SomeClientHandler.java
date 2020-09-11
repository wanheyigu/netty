package com.zxy.client;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.ScheduledFuture;
/*
 * 业务场景中不需要读数据，所以继承ChannelInboundHandlerAdapter
 */
public class SomeClientHandler extends ChannelInboundHandlerAdapter{
	
	private GenericFutureListener listener;
	private ScheduledFuture<?> schedule;

	//当Channel被激活后(即客户端连接服务端)触发该方法执行
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		//ctx.channel().writeAndFlush("~PING~");
		//随机发生心跳
		randomSendHeartBeat(ctx.channel());
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
				/*
				 * 此处的channel.closeFuture()并不是关闭，其意义是：
				 * 若channel被关闭，则会触发该future的监听器的回调方法的执行；
				 * 通道已经被server关闭了，而closeFuture()的作用是在通道关闭时
				 * 执行一个异步调用；例如，关闭发送成功执行递归监听的功能可以在此处执行。
				 * 
				 * ChannelFuture closeFuture = channel.closeFuture();
						closeFuture.addListener((future -> {
							//在此调用一样可以在通道关闭的时候触发删除
							schedule.removeListener(listener);
				   }));
				 * 
				 */
				channel.closeFuture();
				
			}
			
		}, hearBeatInternal, TimeUnit.SECONDS);
		
		//为异步定时任务添加监听器，来实现递归调用，循环向服务端发送心跳
		listener = (future)->{
			/* 《isSuccess()
		     * Returns {@code true} if and only if the I/O operation was completed
		     * successfully.
		     * 当且仅当IO操作成功时返回true(此处判断channel.writeAndFlush("~PING~")执行成功)；》
		     * 
		     * 以上描述是错误的，前期写代码是考虑不周全的结果，实际在代码执行过程中isSuccess()不论channel.writeAndFlush()
		     * 成功还是没执行，都会调用下面的递归方法，所以，其实此处if(future.isSuccess())所判断是否执行成功是判断的
		     * schedule = channel.eventLoop().schedule(() ->  定时器是否成功，而定时任务无论执行if或是else
		     * 都会执行成功，也就全都会返回true，所以此处应用isSuccess()无用；
		     * 
		     * 若异步定时任务执行成功，则重新再次随机发送心跳
		     */
			//if(future.isSuccess()) {
				/* 递归调用
				 * 此处会存在栈溢出问题，即使通道关闭了，递归还是会不断执行；
				 * 因此需要修改，当通道关闭后，删除监听；
				 */
				randomSendHeartBeat(channel);
			//}
		};
		
		schedule.addListener(listener);
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// 通道关闭后移除监听
		schedule.removeListener(listener);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}
