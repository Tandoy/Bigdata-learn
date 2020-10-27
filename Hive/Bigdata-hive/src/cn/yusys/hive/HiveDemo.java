package cn.yusys.hive;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @author Tangzhi mail:tangzhi8023@gmail.com
 * @description:从Hive表中读取数据
 * @date 2018年11月20日
 */
public class HiveDemo {
	public static void main(String[] args) {
		   try {
			   // 注册类
			Class.forName("org.apache.hive.jdbc.HiveDriver");
			Connection connection = DriverManager.getConnection("jdbc:hive2://192.168.25.136:10000/shizhan","root","199799");
			connection.setAutoCommit(false);
			Statement statement = connection.createStatement();
			String sql = "select count(1) from weblogs";
			ResultSet resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				System.out.println(resultSet.getInt(1));
				/**
                 *此时这里可以将从Hive表中读取并经过统计的数据存放进MySqL中然后用于前端展示
                 **/
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
