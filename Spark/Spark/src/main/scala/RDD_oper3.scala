import org.apache.spark.{SparkConf, SparkContext}

object RDD_oper3 {
  def main(args: Array[String]): Unit = {
    //创建SparkConf()并设置App名称
    val conf = new SparkConf().setAppName("RDD_oper").setMaster("local[*]")
    //创建SparkContext，该对象是提交spark App的入口
    val sc = new SparkContext(conf)
    //创建RDD
    val rdd = sc.makeRDD(1 to 10)
    //根据分区好进行数据处理
    val mapPartitionsWithIndexRdd = rdd.mapPartitionsWithIndex {
      case (num, datas) => {
        datas.map(x => {
          x + "：分区" + num
        })
      }
    }
    //收集打印
    mapPartitionsWithIndexRdd.collect().foreach(println)
    //停止RDD
    sc.stop()
  }
}
