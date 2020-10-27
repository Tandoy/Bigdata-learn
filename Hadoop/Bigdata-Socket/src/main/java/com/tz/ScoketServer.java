package com.tz;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Socket服务端
 * @author Administrator
 *
 */
public class ScoketServer {

	public static void main(String[] args) throws IOException {
		//1.得到socket对象
		ServerSocket serverSocket = new ServerSocket();
		serverSocket.bind(new InetSocketAddress("localhost", 8899));
       //2.接受客户端请求
		while(true){
			Socket socket = serverSocket.accept();
			//3.执行业务逻辑层(利用线程控制实现顺序)
			new Thread(new SocketServerTask(socket)).start();
		}
		
	}

}
