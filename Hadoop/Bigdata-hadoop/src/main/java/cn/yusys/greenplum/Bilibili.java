package cn.yusys.greenplum;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Tangzhi mail:tangzhi8023@gmail.com
 * @description:从greenplum数据库中分析处理存储至Mysql数据库
 * @date 2019年3月16日
 */
public class Bilibili {
	static Connection connection = null;
	static Statement statement = null;
	static ResultSet resultSet = null;
	static Connection mysqlConnection = null;
	static Statement mysqlStatement = null;
     public static void main(String[] args) {
    	 JdbcFromGPUtil jdbcFromGPUtil = new JdbcFromGPUtil();
    	 mysqlConnection = new ConnectionPool().getConnection();
		try {
			connection = jdbcFromGPUtil.getConnection();
			statement = connection.createStatement();
			mysqlStatement = mysqlConnection.createStatement();
			mysqlConnection.setAutoCommit(false);
			resultSet = statement.executeQuery("select * from bilibili order by view desc limit 100");
			int beforeTime = (int) System.currentTimeMillis();
			while (resultSet.next()) {
				 String sql = "insert into bilibili_pro(aid,views,danmaku,reply,favorite,coin,share) VALUES ('"+resultSet.getInt(1)+"','"+resultSet.getInt(2)+"','"+resultSet.getInt(3)+"','"+resultSet.getInt(4)+"',"
				 		+ "'"+resultSet.getInt(5)+"','"+resultSet.getInt(6)+"','"+resultSet.getInt(7)+"')";
				 mysqlStatement.execute(sql);
				 mysqlConnection.commit();
			}
			System.out.println(((int)System.currentTimeMillis() - beforeTime)+"ms");
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				// 归还连接至连接池(statement、resultSet应当在归还连接方法中进行显示关闭 不然当大量连接对象创建后会报错OutOfMemoryException)
				jdbcFromGPUtil.closeConnection(connection, statement, resultSet);
				if (mysqlStatement != null) {
					mysqlStatement.close();
				}
				new ConnectionPool().returnConnection(mysqlConnection);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
