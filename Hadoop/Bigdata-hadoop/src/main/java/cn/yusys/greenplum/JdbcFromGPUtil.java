package cn.yusys.greenplum;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * greenplum获取数据库连接工具类
 */
public class JdbcFromGPUtil {
    private ConnPool connPool = new ConnPool();
    /**
     * 获取连接
     */
    public  Connection getConnection() throws SQLException{
        return connPool.getConnection();
    }
   /**
    * 关闭连接
 * @throws SQLException 
    */
    public void closeConnection(Connection conn,Statement st,ResultSet re) throws SQLException {
    	try {
			connPool.closeConnection(conn, st, re);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
