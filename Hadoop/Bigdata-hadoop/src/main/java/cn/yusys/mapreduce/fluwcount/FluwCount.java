package cn.yusys.mapreduce.fluwcount;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * 统计用户上行流量 下行流量分布式程序
 * @author Administrator
 */
public class FluwCount {
	/**
	 * Text: 当前用户手机号
	 * FlueBean: 封装的上行流量 下行流量 实体类(实现hadoop序列化接口)
	 * @author Administrator
	 */
	static class FluwCountMapper extends Mapper<LongWritable, Text, Text, FluwBean>{
		Text k = new Text();
		FluwBean fluwBean = new FluwBean();
		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			// 从HDFS中截取当前用户手机号 上行流量 下行流量
			 String line = value.toString();
			 String[] fields = line.split("\t");
			 // 手机号
			 String phoneNbr = fields[1];
	         // 上行流量 下行流量
			 long upFluw = Long.parseLong(fields[fields.length - 3]);
			 long downFluw = Long.parseLong(fields[fields.length - 2]); 
			 /**
			  * 写出交给reduce程序
			  * key : 用户手机号
			  * value : 上行流量 下行流量 实体类
			  */
			 k.set(phoneNbr);
			 fluwBean.set(upFluw, downFluw);
			 context.write(k,fluwBean);
		}
	}
	/**
	 * @author Administrator
	 *  入参 : 用户手机号   上行流量 下行流量 实体类
	 *  输出 : 用户手机号    总流量数
	 */
   static class FluwCountReduce extends Reducer<Text, FluwBean, Text, FluwBean>{
	   FluwBean resultFluwBean = new FluwBean();
	   @Override
	   protected void reduce(Text key, Iterable<FluwBean> values,
			Context context) throws IOException, InterruptedException {
		   long sum_upFluw = 0;
		   long sum_downFluw = 0;
		   //  同一电话号码 调用reduce方法 上行流量与下行流量相加
		   for (FluwBean fluwBean : values) {
			 sum_upFluw += fluwBean.getUpFluw();
			 sum_downFluw = fluwBean.getDownFluw();
		}
		   resultFluwBean.set(sum_upFluw, sum_downFluw);
		   context.write(key, resultFluwBean);
	 }
   }
   
   public static void main(String[] args) throws Exception {
	   Configuration conf = new Configuration();
		Job wcjob = Job.getInstance(conf);
		// 指定我这个job所在的jar包
        // wcjob.setJar("/home/hadoop/wordcount.jar");
		wcjob.setJarByClass(FluwCount.class);
		wcjob.setMapperClass(FluwCountMapper.class);
		wcjob.setReducerClass(FluwCountReduce.class);
		// 设置自动分区算法(自定义逻辑分区类继承Partitioner)
		wcjob.setPartitionerClass(ProvincePartitioner.class);
//		// 设置分布式计算分区数 此时就会生成五个结果统计文件part-0000  00001  00002......
//		wcjob.setNumReduceTasks(5);
		// 设置我们的业务逻辑Mapper类的输出key和value的数据类型
		wcjob.setMapOutputKeyClass(Text.class);
		wcjob.setMapOutputValueClass(FluwBean.class);
		// 设置我们的业务逻辑Reducer类的输出key和value的数据类型
		wcjob.setOutputKeyClass(Text.class);
		wcjob.setOutputValueClass(FluwBean.class);
		// 指定要处理的数据所在的位置
		FileInputFormat.setInputPaths(wcjob, new Path(args[0]));
		// 指定处理完成之后的结果所保存的位置
		FileOutputFormat.setOutputPath(wcjob, new Path(args[1]));
		// 向yarn集群提交这个job
		boolean res = wcjob.waitForCompletion(true);
		System.exit(res?0:1);
  }
}
