import java.util.{Properties, Random}

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011

/**
 * 1.消费kafka数据
 * 2.与hive离线数据进行统一计算
 * 3.写回kafka或者落盘
 */

case class UU(area:String,uid:String,os:String,ch:String,appid:String,mid:String,type2:String,vs:String,ts:String)
case class UU2(id: String, timestamp: Long, temperature: Double)
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
    val stream2 = environment.addSource(new FlinkKafkaConsumer011[String]("GMALL_STARTUP", new SimpleStringSchema(), properties))
    //    stream2.print()
    //    environment.execute("test-kafka")

    // 3.自定义source，模拟hive数据/flink-->catalog读取hive数据
    val stream3 = environment.addSource(new HiveSensorSource())
    stream3.print()
    environment.execute("test-selfSource")
  }
}

class HiveSensorSource extends SourceFunction[UU2]{
  // 控制是否继续读取数据的标志 表示数据源是否还在正常运行
  var  runFlag = true
  override def run(sourceContext: SourceFunction.SourceContext[UU2]): Unit = {
    // 初始化一个随机数发生器
    val rand = new Random()
    var curTemp = 1.to(10).map(
      i => ("sensor_" + i, 65 + rand.nextGaussian() * 20)
    )
    while (runFlag) {
      // 更新温度值
      curTemp = curTemp.map(
        t => (t._1, t._2 + rand.nextGaussian())
      )
      // 获取当前时间戳
      val curTime = System.currentTimeMillis()
      curTemp.foreach(
        t => sourceContext.collect(UU2(t._1, curTime, t._2))
      )
      Thread.sleep(100)
    }
  }
  // 停止
  override def cancel(): Unit = {
    runFlag = false
  }
}
