package com.zxy.rpc_client.client.discovery;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.zxy.rpc_api.pojo.ZKConstant;

/** 
 * 服务发现实现类
 */

public class ServiceDiscoveryImpl implements ServiceDiscovery{

	//zk客户端
	private CuratorFramework curator;
	
	/* 子节点列表
	 * 思考：此处子节点列表定义是否要考虑线程安全问题?
	 *    1.在此项目工程中不用考虑线程安全问题，因为ServiceDiscoveryImpl本身是个多例，所有无需考虑；
	 *    
	 *    2.如果交由spring管理，那么需要考虑线程安全问题，同时其存在线程安全问题，
	 *    根据逻辑，当A线程进入，获取的servers列表为3个服务提供者，B线程运行触发
	 *    了watcher监听事件，servers列表变更为5个服务提供者，那么两个servers
	 *    出现了不同值，并且B触发监听后会将servers更改为5，这就出现了线程安全问题；
	 *    但是此种线程安全问题对于工程相对有利，既然服务列表已经发生了变化，那么就应该
	 *    即时变更，而不是因为存在线程问题就限制操作，所有线程安全在此需要考虑，但根据
	 *    项目的实际运行情况并不需要应用线程安全的集合来解决。
	 */
	private List<String> servers;
	
	
	//1. 连接zk
	public ServiceDiscoveryImpl() {
		this.curator = CuratorFrameworkFactory.builder()
				.connectString(ZKConstant.ZK_CLUSTER)
				.connectionTimeoutMs(10000)
				.sessionTimeoutMs(4000)
				.retryPolicy(new ExponentialBackoffRetry(1000, 10))
				.build();
		curator.start();
	}
	
	@Override
	public String discovery(String serviceName) throws Exception {
		/* 
		 * 2.获取指定服务名称子节点列表  
		 */
		String servicePath = ZKConstant.ZK_DUBBO_ROOT_PATH + "/" +serviceName;
		
		servers = curator.getChildren().forPath(servicePath);
		
		//如果没有服务提供者，直接返回
		if(servers.size() == 0) {
			return null;
		}
		
		/*
		 * 3.添加watcher监听，监听子节点列表变化
		 *    此功能在此项目中没有用处，即便列表变化了，对于现有程序也没用处，程序已经
		 *    获取到了服务提供者，并且已经调用了，而且就调用一次，列表变化也不会影响到现状；
		 *    
		 *    如果开发的是web工程，就需要watcher监听，监听子节点列表变化，因为在web中
		 *    一个消费者可以提交N次，列表的变化就会影响到后期的提交；
		 */
		registerWatcher(servicePath);
		/*
		 * 4.获取服务提供者，对服务提供者列表进行负载均衡；
		 */
		String server = new RandomLoadBalanceImpl().choose(servers);
		
		
		return server;
	}

	private void registerWatcher(String servicePath) throws Exception {
		/*
		 * PathChildrenCache会将指定路径下的节点数据内容及状态缓存到本地
		 * 注：其获取data数据内容无法保证同步性，因此推荐添加监听器，一旦变化就会监听到
		 */
		PathChildrenCache cache = new PathChildrenCache(curator, servicePath, true);
		/*
		 * 监听：被监听的地址一旦出现变化，就会被监听到，然后触发PathChildrenCacheListener中
		 * 的childEvent(CuratorFramework client, PathChildrenCacheEvent event)
		 * 方法，这样就可以通过在childEvent中获取当前的子节点列表；
		 * 
		 * 此功能目的：
		 *    一旦监听到子节点列表发送变化，马上更新当前的servers集合
		 * 注：监听的方式获取data数据内容更好，其实时、准确；
		 */
		cache.getListenable().addListener((client,event) -> {//使用Lambda表达式方式
			servers = curator.getChildren().forPath(servicePath);
		});
		//启动监听
		cache.start();
		
		/* 正常应用PathChildrenCacheListener来进行监听
		cache.getListenable().addListener(new PathChildrenCacheListener() {
			
			@Override
			public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
				// TODO Auto-generated method stub
				
			}
		});*/
		
	}

}
