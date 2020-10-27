package cn.yusys.greenplum;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Properties;

/**
 * 数据库连接池Java原生版
 * Created on 2018-11-15
 * @author @author tangzhi mail:tangzhi8023@gmail.com
 */
public class ConnectionPool {
           private static LinkedList<Connection> connectionQueue;
           private static Properties prop ;

    /**
     * 驱动类
     */
    static {
        try {
            prop  = new Properties();
            prop.load(new FileInputStream(new File("D:\\java\\Bigdata-parent\\Bigdata-hadoop\\src\\main\\java\\Mysqldb.properties")));
            Class.forName(prop.getProperty("driverName").toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 获取连接对象
     */
    public synchronized Connection getConnection () {
        if (connectionQueue == null || connectionQueue.size() == 0) {
            connectionQueue = new LinkedList<Connection>();
            for (int i = 0;i < 5;i ++) {
                try {
                    Connection connection = DriverManager.getConnection(prop.getProperty("url").toString(), prop.getProperty("username").toString(), prop.getProperty("password").toString());
                    connectionQueue.add(connection);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return connectionQueue.poll();
    }
    /**
     * 归还连接至连接池
     */
    public void returnConnection(Connection connection) {
        connectionQueue.add(connection);
    }
}
