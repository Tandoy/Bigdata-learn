package com.tz;

import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

/**
 * 分布式服务上下线动态感知客户端
 * @author Administrator
 *
 */
public class DistributeClient {
	private static final String CONNECT_STRING = "192.168.25.132:2181,192.168.25.132:2182,192.168.25.132:2183";
	private static final int SESSION_TIMEOUT = 2000;
	private  ZooKeeper zkClient = null;
	//volatile:使多个业务线程操作一个服务器列表集合
	private volatile List<String> serversList;
	private static String parentNode = "/servers";
	/**
	 * 获取zookeeper连接
	 * @throws Exception
	 */
	public  void getConnection() throws Exception{
	      zkClient = new ZooKeeper(CONNECT_STRING, SESSION_TIMEOUT, new Watcher() {	
			public void process(WatchedEvent event) {
				System.out.println(event.getType()+event.getPath());
				try {
					//监听重新获取服务器列表
					getServerList();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	/**
	 * 得到已在zookeeper上注册的服务器列表
	 * @param args
	 * @throws InterruptedException 
	 * @throws KeeperException 
	 */
	public void getServerList() throws KeeperException, InterruptedException{
		List<String> children = zkClient.getChildren(parentNode, true);
		List servers = new ArrayList<String>();
		for (String chi : children) {
			//得到子节点下的内容
			byte[] data = zkClient.getData(parentNode+"/"+chi,false, null);
			//把子节点所有服务器列表放入集合中
			servers.add(new String(data));
		}
		serversList = servers;
		System.out.println(serversList);
	}
	/**
	 * 执行业务线程
	 * @param args
	 * @throws Exception
	 */
	public void handleBusiness() throws Exception{
		System.out.println("client start working");
		Thread.sleep(Long.MAX_VALUE);
	}
	public static void main(String[] args) throws Exception {
		 //1.获取zk连接
		DistributeClient client = new DistributeClient();
		client.getConnection();
		//2.得到已在zookeeper上注册的服务器列表
		client.getServerList();
		//3.执行业务线程
		client.handleBusiness();
	}

}
