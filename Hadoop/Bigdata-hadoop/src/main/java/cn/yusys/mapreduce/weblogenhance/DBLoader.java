package cn.yusys.mapreduce.weblogenhance;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

public class DBLoader {

	public static void dbLoader(Map<String, String> ruleMap) throws Exception {

		Connection conn = null;
		Statement st = null;
		ResultSet res = null;
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/hadoopurldb", "root", "root");
			st = conn.createStatement();
			res = st.executeQuery("select url,content from url_rule");
			while (res.next()) {
				ruleMap.put(res.getString(1), res.getString(2));
			}

		} finally {
			try{
				if(res!=null){
					res.close();
				}
				if(st!=null){
					st.close();
				}
				if(conn!=null){
					conn.close();
				}

			}catch(Exception e){
				e.printStackTrace();
			}
		}

	}

}
