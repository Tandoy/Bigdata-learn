import org.apache.spark.{SparkConf, SparkContext}

object WordCount {
  def main(args: Array[String]): Unit = {
    //创建SparkConf()并设置App名称
    val conf = new SparkConf().setAppName("WC").setMaster("local[*]")
    //创建SparkContext，该对象是提交spark App的入口
    val sc = new SparkContext(conf)
    //使用sc创建RDD并执行相应的transformation和action
    sc.textFile("in/a.txt").flatMap(_.split(" "))
      .map((_, 1)).reduceByKey(_+_, 1).sortBy(_._2, false)
      .saveAsTextFile("out")
    //停止sc，结束该任务
    sc.stop()
  }
}
