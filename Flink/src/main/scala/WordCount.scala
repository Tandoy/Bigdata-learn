import org.apache.flink.api.scala._

//基于flink的批处理操作
object WordCount {
  def main(args: Array[String]): Unit = {
     //1.得到执行环境
     val env = ExecutionEnvironment.getExecutionEnvironment
    //2.数据存放地址
    val inputpath = "D:\\Downloads\\github\\Bigdata-learn\\Flink\\src\\main\\resources\\wc.txt"
    //3.读取文件数据
    val dataSet = env.readTextFile(inputpath)
    //4.进行wordcount操作
    val wcSet = dataSet.flatMap(_.split(" "))
      .map((_, 1))
      .groupBy(0)
      .sum(1)
    //5.打印
    wcSet.print()
  }
}
