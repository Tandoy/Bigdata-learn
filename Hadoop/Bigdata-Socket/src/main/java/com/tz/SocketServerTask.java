package com.tz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
/**
 * 业务逻辑实现
 * @author Administrator
 *
 */
public class SocketServerTask implements Runnable{
    Socket socket ;
    InputStream in = null;
    BufferedReader br = null;
    OutputStream out = null;
    PrintWriter pw = null;
	public SocketServerTask(Socket socket) {
		this.socket = socket;
	}

	public void run() {
	      //1.得到客户端发送的请求参数
		  try {
		    out = socket.getOutputStream();
		    in = socket.getInputStream();
		    pw = new PrintWriter(out);
			//转换流
			br = new BufferedReader(new InputStreamReader(in));
			String parm  = br.readLine();
			//2.反射得到具体业务逻辑实现类
			@SuppressWarnings("unchecked")
			Class<GetDataServerImpl> Clazz = (Class<GetDataServerImpl>) Class.forName("com.tz.GetDataServerImpl");
			GetDataServerImpl getDataServerImpl = Clazz.newInstance();
			String data = getDataServerImpl.getData(parm);
			//3.返回客户端请求数据
			pw.println(data);
			pw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			//4.释放资源
			if ( in != null ) {
				try {
					in.close();
					if ( br != null ){
						br.close();
					}
					   if (pw != null ){
						   pw.close();
					   }
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
