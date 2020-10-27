package cn.yusys.greenplum;

import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * @author Tangzhi mail:tangzhi8023@gmail.com
 * @description:自定义数据库连接池
 * @date 2019年3月13日
 */
public class ConnPool implements DataSource{
    // 1.使用LinkedList模拟
	private static LinkedList<Connection> linkedList = new LinkedList<Connection>();
	// 2.在静态代码块中加载数据库连接配置文件
	static {
		InputStream inputStream = ConnPool.class.getClassLoader().getResourceAsStream("db.properties");
		Properties prop = new Properties();
		try {
			prop.load(inputStream);
			String driver = prop.getProperty("driver");
            String url = prop.getProperty("url");
            String user = prop.getProperty("user");
            String password = prop.getProperty("password"); 
            // 3.数据库连接池的初始化连接数的大小
            int  initSize = Integer.parseInt(prop.getProperty("InitSize"));
            // 4.加载驱动
            Class.forName(driver);
            // 5.根据配置文件中初始连接数创建
            for (int i = 0 ; i < initSize ; i++) {
            	Connection connection = DriverManager.getConnection(url, user, password);
            	// 6.将创建的连接放入连接池
            	linkedList.add(connection);
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public PrintWriter getLogWriter() throws SQLException {
		return null;
	}

	public int getLoginTimeout() throws SQLException {
		return 0;
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return null;
	}

	public void setLogWriter(PrintWriter arg0) throws SQLException {
	}

	public void setLoginTimeout(int arg0) throws SQLException {
	}

	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		return false;
	}

	public <T> T unwrap(Class<T> arg0) throws SQLException {
		return null;
	}
     
	/**
	 * 从数据库连接池中获取连接
	 */
	public Connection getConnection() throws SQLException {
		if (linkedList.size() > 0) {
			// 从集合中获取一个连接
			final Connection connection = linkedList.removeFirst();
			// 返回connection代理对象
			return connection;
		} else {
			throw new RuntimeException("数据库连接池繁忙,请稍后再试!");
		}
	}
    public void closeConnection (Connection conn,Statement st,ResultSet re) throws Exception {
    	if (st != null) {
    		st.close();
    	}
    	if (re != null) {
    		re.close();
    	}
    	if (conn != null) {
    		// 返回数据库连接池
    		linkedList.add(conn);
    	}
    }
	public Connection getConnection(String username, String password) throws SQLException {
		return null;
	}
}
