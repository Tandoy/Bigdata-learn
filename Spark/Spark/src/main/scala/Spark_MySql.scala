import org.apache.spark.rdd.JdbcRDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark_MySql {
  def main(args: Array[String]): Unit = {
      /*spark连接mysql*/
      //创建SparkConf()并设置App名称
      val conf = new SparkConf().setAppName("WC").setMaster("local[*]")
    //创建SparkContext，该对象是提交spark App的入口
    val sc = new SparkContext(conf)
    val driver = "com.mysql.jdbc.Driver"
    val url = "jdbc:mysql://bigData:3306/hotnews"
    val user = "root"
    val password = "root"
    val sql = "select name,age from test where id >= ? and id <= ?"
    val jdbcRDD = new JdbcRDD(
      sc,
      () => {
        Class.forName(driver)
        java.sql.DriverManager.getConnection(url, user, password)
      },
      sql,
      1,
      10,
      2,
      (rs) => {
        println(rs.getString(1) + "," + rs.getInt(2))
      }
    )
    jdbcRDD.collect()
  }
}
