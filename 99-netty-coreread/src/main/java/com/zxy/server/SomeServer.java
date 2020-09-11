package com.zxy.server;

import io.netty.channel.socket.SocketChannel;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ReflectiveChannelFactory;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.AttributeKey;
/**
 * netty源码解析---Netty服务端启动
 * 1-Channl的创建
 * 2-Channel的初始化
 * 3-将Channel注册到Selector
 * 4-绑定端口激活Channel
 */
public class SomeServer {

	public static void main(String[] args) throws InterruptedException {
		/*NioEventLoop解析
		 * EventLoop本质是一个EventExecutor事件执行器
		 */
		EventLoopGroup parentGroup = new NioEventLoopGroup();
		EventLoopGroup childGroup = new NioEventLoopGroup();
		
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(parentGroup, childGroup)
						
					 /* 1.2 定义channel类型，并通过传入的类型创建了一个反射的ChannelFactory，即1.1channel创建是应用的反射工厂，
					  * return channelFactory(new ReflectiveChannelFactory<C>(channelClass));
					  * 在new ReflectiveChannelFactory<C>(channelClass)中获取了出入的NioServerSocketChannel.class
					  * 的构造器，并赋值this.constructor = clazz.getConstructor()，所已1.1中的constructor即
					  * NioServerSocketChannel.class类型的channel的构造器；
					  * 
					  * 1.3 NioServerSocketChannel的创建过程是，首先创建了一个JDK的Channel，然后为其创建了一个id、channelPiple
					  * 及config配置类；
					  * 
					  * 
					  * 5.2 NioServerSocketChannel的无参构造器创建pipeline
					  */
					 .channel(NioServerSocketChannel.class)
					 
					 /* 2.2 初始化options，将所有的option添加到1.3中的config里，然后配置给channel;
					  *    初始化Attrs,连接属性attrs和业务属性childAttrs，添加到config中，配置给channel;
					  * 
					  */
					 .option(ChannelOption.SO_KEEPALIVE, true)
					 //.attr("配置属性", value)
					 .childAttr(AttributeKey.valueOf("配置业务处理器属性"), "可以在自定义处理器中通过ctx获取")
					 
					 .handler(null)//处理连接请求的处理器，服务器端一般不配置
					 /*
					  * 此处也是向pipeline中添加处理器ChannelInitializer：
					  *    如果ChannelInitializer处理器添加成功，那么就会执行其匿名内部类中的initChannel(SocketChannel ch)方法内部的添加逻辑。
					  *    执行完成后就完成了ChannelInitializer处理器的使命，其作用就是运行内部类逻辑(将其他处理器添加到pipeline)，使命完成后删除当前处理器ChannelInitializer。
					  */
					 .childHandler(new ChannelInitializer<SocketChannel>() {

						 /* 2.3 将option、attr、childOption、childAttr，及新创建的一个专门用于处理连接请求的处理器初始化到channel中(实际是添加到channelPipeline中)。
						  * 
						  */
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							/* pipeline本质是一个双向链表：
							 *   pipeline中装载的是一个个结点，每个处理器会被封装为一个结点，
							 *   其初始化时会自动创建头结点和尾节点，然后载入处理器结点，其结点从
							 *   头结点开始收尾相连即，当前节点的next就是下一个结点，而且每个
							 *   结点都可以找到自己的上一个结点，因此其为双向链表。
							 * 
							 * pipeline中的节点是ChannelHandlerContext，
							 * 经常使用的是接口的实现类AbstractChannelHandlerContext
							 * 
							 * 5.pipeline的创建是在bind(8888)时创建，追踪
							 */
							ChannelPipeline pipeline = ch.pipeline();
							//StringDecoder：字符串解码器，将channel中的ByteBuf数据解码为String
							pipeline.addLast(new StringDecoder());
							//StringEncoder：字符串编码器，将String编码为将要发送到channel中的ByteBuf
							pipeline.addLast(new StringEncoder());
							pipeline.addLast("SomeServerHandle", new SomeServerHandle());
						}
					});
			/* 1.1Channl的创建
			 *   channel是在绑定的时候创建，在AbstractBootstrap中的initAndRegister()方法通过channel = channelFactory.newChannel()工厂创建；
			 *   .newChannel()方法应用的是反射工厂ReflectiveChannelFactory，其直接return constructor.newInstance()，即通过反射调用无参构造器，
			 *   constructor就是channel，此处是自定义配置的channel类型NioServerSocketChannel.class；
			 *   
			 *   
			 * 2.1初始化Channel，在绑定时先执行创建，而后执行初始化init(channel);
			 * 
			 * 
			 * 3.1将Channel注册到selector
			 *    ChannelFuture regFuture = config().group().register(channel);
			 *    
			 * 4.1绑定端口
			 *    doBind0(regFuture, channel, localAddress, promise);
			 *    激活channel，并触发channelActive()方法的执行。
			 * 
			 */
			ChannelFuture future = bootstrap.bind(8888).sync();
			
			
			
			
			System.out.println("服务器已启动！");
			future.channel().closeFuture().sync();
		}finally {
			parentGroup.shutdownGracefully();
			childGroup.shutdownGracefully();
			
		}
	}
}
