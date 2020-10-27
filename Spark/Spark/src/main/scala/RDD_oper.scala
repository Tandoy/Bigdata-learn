import org.apache.spark.{SparkConf, SparkContext}

object RDD_oper {
  def main(args: Array[String]): Unit = {
    //创建SparkConf()并设置App名称
    val conf = new SparkConf().setAppName("RDD_oper").setMaster("local[*]")
    //创建SparkContext，该对象是提交spark App的入口
    val sc = new SparkContext(conf)
    //创建RDD
    val rdd = sc.makeRDD(1 to 10)
    //进行map算子操作
    val mapRdd = rdd.map(x => {
      x * 2
    })
    //收集打印
    mapRdd.collect().foreach(println)
    //停止RDD
    sc.stop()
  }
}
