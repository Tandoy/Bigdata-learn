package com.tz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *  Socket客户端
 * @author Administrator
 *
 */
public class SocketClient {
	public static void main(String[] args) throws IOException {
		//1.建立连接
		Socket socket = new Socket("localhost", 8899);
		//2.获取输出输入流
		InputStream in = socket.getInputStream();
		OutputStream out = socket.getOutputStream();
		PrintWriter pw = new PrintWriter(out);
		pw.println("hello");
		pw.flush();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String data = br.readLine();
		System.out.println(data);
		//3.关闭资源
		in.close();
		out.close();
		socket.close();
	}

}
