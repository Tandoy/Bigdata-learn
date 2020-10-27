package cn.yusys.greenplum;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Tangzhi mail:tangzhi8023@gmail.com
 * @description:测试自定义数据库连接池
 * @date 2019年3月13日
 */
public class TestJDBCGPConnPool {
	static Connection connection = null;
	static Statement statement = null;
	static ResultSet resultSet = null;
     public static void main(String[] args) {
    	 JdbcFromGPUtil jdbcFromGPUtil = new JdbcFromGPUtil();
		try {
			connection = jdbcFromGPUtil.getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery("select * from bilibili limit 20");
			while (resultSet.next()) {
				 // 打印犯罪具体位置信息(经纬度)
				 System.out.println(resultSet.getString(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				// 归还连接至连接池(statement、resultSet应当在归还连接方法中进行显示关闭 不然当大量连接对象创建后会报错OutOfMemoryException)
				jdbcFromGPUtil.closeConnection(connection, statement, resultSet);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
