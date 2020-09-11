package com.zxy.rpc_server.registry.impl;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.stereotype.Component;

import com.zxy.rpc_api.pojo.ZKConstant;
import com.zxy.rpc_server.registry.RegistryCenter;
/** 注册实现类
 * 
 */
@Component
public class ZKRegistryCenterImpl implements RegistryCenter{

	/* 声明zk客户端
	 * 连接zk需要通过，curator客户端；
	 * 一创建ZKRegistryCenterImpl对象实例，zk客户端就被创建完成。
	 */
	private CuratorFramework curator;
	
	//实例代码块
	{
		curator = CuratorFrameworkFactory.builder()
				//连接zk集群，参数为地址
				.connectString(ZKConstant.ZK_CLUSTER)
				//连接超时时间，默认15秒
				.connectionTimeoutMs(10000)
				//会话超时时间，默认60秒
				.sessionTimeoutMs(4000)
				//重试机制:没一秒重试一次，最大重试10次
				.retryPolicy(new ExponentialBackoffRetry(1000, 10))
				.build();
		//启动zk客户端
		curator.start();
				
	}
	
	@Override
	public void register(String serviceName, String serviceAddress) throws Exception {
		/*
		 * 创建服务节点(持久化节点)：
		 *    类似于/dubbocustom/com.zxy.rpc.service.SomeService的节点
		 */
		String servicePath = ZKConstant.ZK_DUBBO_ROOT_PATH +"/" + serviceName;
		
		
		/* 节点不存在则创建
		 *    zk的节点具有唯一性原则，可以不加此判断，此处加上判断在于提升效率，
		 *    在不加判断时，还会执行create()方法，只是在执行过程中出现了异常，
		 *    因此加判断要比执行create()方法消耗的资源少。
		 * 注：if(curator.checkExists().forPath(servicePath) == null)
		 *    此判断是需要通过网络，到达ZK进行检测；
		 */
		if(curator.checkExists().forPath(servicePath) == null) {
			/*
			 * 1.创建服务名称的持久节点
			 */
			curator.create()
					//如果父节点不存在,则创建父节点
					.creatingParentsIfNeeded()  
					//创建持久节点
					.withMode(CreateMode.PERSISTENT)
					.forPath(servicePath,"0".getBytes());//第二个参数为节点内容，可以不用写
		}
		/*
		 * 2.创建ip+port的临时节点
		 *      类似于/dubbocustom/com.zxy.rpc.service.SomeService/192.168.85.129:8001
		 */
		String addressPath = servicePath + "/" + serviceAddress;
		
		String nodeName = curator.create()
				.withMode(CreateMode.EPHEMERAL)
				.forPath(addressPath,"0".getBytes());
		
		System.out.println("提供者主机节点创建成功："+nodeName);
	}

}
