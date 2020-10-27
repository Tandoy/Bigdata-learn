package com.tz;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * 测试hadoop hdfs
 * @author Administrator
 *
 */
public class HadoopHdfsCilent {
   FileSystem fs = null;
   Configuration conf = null;
   @Before
   public void init() throws Exception{
	   //默认同样调用有参true的方法
	   Configuration conf = new Configuration(); 
	   // 设置副本个数
//	   conf.set("dfs.replication", "2");
	   conf.set("fs.defaultFS", "hdfs://192.168.25.11:9000");
	// 获取hdfs客户端操作实例对象
	// 在windows下JAVA API远程操作hadoop HDFS文件系统时应注意用户认证权限
	   fs = FileSystem.get(new URI("hdfs://192.168.25.11:9000"), conf, "root");
   }
   /**
    * HDFS文件上传
    * @throws Exception
    * @throws IOException
    */
   @Test
   public void testUpload() throws Exception, IOException{
	   fs.copyFromLocalFile(new Path("D:/gp-hdfs.txt"), new Path("/gp-hdfs.txt"));
	   fs.close();
   }
   /**
    * HDFS文件下载
 * @throws IOException 
 * @throws IllegalArgumentException 
    */
    @Test
    public void testdownLoad() throws IllegalArgumentException, IOException{
    	fs.copyToLocalFile(new Path("/test.txt"), new Path("D:/"));
    	// 关闭资源
    	fs.close();
    }
    /**
     * HDFS文件增删改查
     * @throws IOException 
     * @throws IllegalArgumentException 
     */
    @Test
    public void testMkidr() throws IllegalArgumentException, IOException{
    	// 创建目录
    	boolean mkdirs = fs.mkdirs(new Path("/tz/test/kobe"));
    	fs.close();
    	System.out.println(mkdirs);
    }
    
	@Test
    public void testDelete() throws IllegalArgumentException, IOException{
    	// 删除文件
		// 第二个参数为当时文件夹是 执行递归删除
    	boolean delete = fs.delete(new Path("/kobe.txt"),false);
    	fs.close();
    	System.out.println(delete);
    }
	
	@Test
	public void renameFile() throws IllegalArgumentException, IOException {
	    // 修改文件名称
		fs.rename(new Path("/kobe2.txt"), new Path("/kobe.txt"));
		// 关闭资源
		fs.close();
	}
	
    @Test
    public void testLs() throws FileNotFoundException, IllegalArgumentException, IOException{
    	//  获取文件详情(采用迭代器的方式进行遍历)
    	RemoteIterator<LocatedFileStatus> listFiles = fs.listFiles(new Path("/"), true);
    	while(listFiles.hasNext()){
    		LocatedFileStatus fileStatus = listFiles.next();
    		// 文件名称
    		System.out.println(fileStatus.getPath().getName());
    		// 快大小
			System.out.println(fileStatus.getBlockSize());
			// 权限
			System.out.println(fileStatus.getPermission());
			// 长度
			System.out.println(fileStatus.getLen());
			// 拥有者
			System.out.println(fileStatus.getOwner());
    	}
    	// 关闭资源
    	fs.close();
    }
    
    @Test
    public void testLs2() throws FileNotFoundException, IllegalArgumentException, IOException{
    	// 判断HDFS中是文件夹还是文件
    	FileStatus[] listStatus = fs.listStatus(new Path("/"));
    	String flag = "d--";     
    	for (FileStatus fileStatus : listStatus) {
    		if (fileStatus.isFile())  
    			flag = "f--";
			System.out.println(flag + fileStatus.getPath().getName());
		}
    	// 关闭资源
    	fs.close();
    }
    
    /**
     * 通过流来控制写入文件大小
     * @throws IOException 
     * @throws IllegalArgumentException 
     */
    @Test
	public void testUpload2() throws IllegalArgumentException, IOException{
    	// 获取输出流
    	FSDataOutputStream outputStream = fs.create(new Path("/kobe2.txt"), true);
    	// 创建输入流
    	FileInputStream inputStream = new FileInputStream(new File("D:/kobe.txt"));
    	// 执行上传
    	IOUtils.copyBytes(inputStream, outputStream, conf);
    	// 关闭资源
    	IOUtils.closeStream(inputStream);
        IOUtils.closeStream(outputStream);
    	fs.close();
    }
    /**
     * 通过IO流定位下载(第一种方式)
     */
    @Test
	public void testRandomAccess() throws IllegalArgumentException, IOException{
    	// 获取输入流
		FSDataInputStream fis = fs.open(new Path("/"));
		// 获取输出流
		FileOutputStream fos = new FileOutputStream(new File("d:/"));
		// 设置读取大小
		byte[] buf = new byte[1024];
		for (int i = 0; i<1024*128; i++)  {
			fis.read(buf);
			fos.write(buf);
		}
		// 关闭资源
		IOUtils.closeStream(fos);
		IOUtils.closeStream(fis);
		fs.close();
	}
    
    /**
     * 通过IO流定位下载(第二种方式)
     * @throws IOException 
     * @throws IllegalArgumentException 
     */
    public void testRandomAccess2() throws IllegalArgumentException, IOException {
    	// 获取输入流
    	FSDataInputStream fis = fs.open(new Path("/"));
    	// 设置偏移量制定位置读取
    	fis.seek(1024*1024*128);
    	// 创建输出流
    	FileOutputStream fos = new FileOutputStream(new File("/"));
    	// 流的对拷
    	IOUtils.copyBytes(fis, fos, conf);
    	// 关闭资源
    	IOUtils.closeStream(fos);
    	IOUtils.closeStream(fis);
    	fs.close();
    }
}

