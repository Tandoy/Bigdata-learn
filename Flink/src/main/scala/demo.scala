import java.util.Properties

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.table.api.EnvironmentSettings
import org.apache.flink.table.api.scala.StreamTableEnvironment
import org.apache.flink.table.catalog.hive.HiveCatalog

/**
 * 1.消费kafka数据
 * 2.与hive离线数据进行统一计算
 * 3.写回kafka或者落盘
 */

case class UU(area:String,uid:String,os:String,ch:String,appid:String,mid:String,type2:String,vs:String,ts:String)
object demo {
  def main(args: Array[String]): Unit = {
     // 1.构建流式执行环境读取集合/文件
     val environment = StreamExecutionEnvironment.getExecutionEnvironment
    val uu1 = List(
      "hebei","252","andriod","wandoujia","gmall2019","mid_342","startup","1.2.0","1610527041861"
    )
    val stream = environment.fromCollection(uu1)
    //stream.print()
    //environment.execute("test")

    // 2.流式处理kafka数据
    val properties = new Properties()
    properties.setProperty("bootstrap.servers","dxbigdata103:9092")
    val stream2 = environment.addSource(new FlinkKafkaConsumer[String]("GMALL_STARTUP", new SimpleStringSchema(), properties))
    stream2.print()
    environment.execute("test-kafka")

    /*// 3.flink读取hive数据
    val name = "test"
    val defaultDatabase = "wcl_dwh"
    val hiveConfDir = "D:\\Downloads\\github\\Bigdata-learn\\Flink\\src\\main\\resources\\conf"
    val version = "1.1.0"
    val bsEnv = StreamExecutionEnvironment.getExecutionEnvironment
    val bsSettings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build()
    val bsTableEnv = StreamTableEnvironment.create(bsEnv, bsSettings)
    val configuration = bsTableEnv.getConfig.getConfiguration
    configuration.setString("table.exec.mini-batch.enabled", "true")
    configuration.setString("table.exec.mini-batch.allow-latency", "5 s")
    configuration.setString("table.exec.mini-batch.size", "5000")
    val catalog = new HiveCatalog(name
      ,defaultDatabase
      ,hiveConfDir
      ,version)
    bsTableEnv.registerCatalog(name,catalog)
    bsTableEnv.useCatalog(name)
    bsTableEnv.sqlQuery("select * from wcl_dwh.stg_card_card_acct limit 5;")
    bsTableEnv.execute("test-flink-hive")*/
  }
}
