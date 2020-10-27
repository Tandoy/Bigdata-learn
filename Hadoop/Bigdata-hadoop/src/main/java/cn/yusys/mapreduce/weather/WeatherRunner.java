package cn.yusys.mapreduce.weather;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * 天气案例
 * @author Tangzhi mail:tangzhi8023@gmail.com
 * @description:
 * @date 2019年9月1日
 */
public class WeatherRunner {
   public static void main(String[] args) throws Exception{
	  Configuration conf = new Configuration();
	  Job job = Job.getInstance(conf);
	  job.setJarByClass(WeatherRunner.class);
	  //------conf------
//	  job.setInputFormatClass(cls); 框架默认为TextInputFormat
	  //----map
	  job.setMapperClass(WeatherMapper.class);
	  job.setMapOutputKeyClass(WeatherBean.class);
	  job.setMapOutputValueClass(IntWritable.class);
	  job.setPartitionerClass(WeatherPartition.class);  //<k,v> --> <k,v,p>
	  job.setSortComparatorClass(WeatherSortComparator.class); //环形缓冲区排序
//	  job.setCombinerClass(WeatherCombiner.class);
	  //----map: end
	  //----Reduce
	  job.setGroupingComparatorClass(WeatherGroupingComparator.class); //Reduce阶段分组排序
	  job.setReducerClass(WeatherReducer.class);
	  //Reduce个数为2
	  job.setNumReduceTasks(2);
	  //input output
	  Path input = new Path("/data/weather/input");
	  FileInputFormat.addInputPath(job, input);
		
	  Path output = new Path("/data/weather/output");
	  if(output.getFileSystem(conf).exists(output)){
		  output.getFileSystem(conf).delete(output,true);
		}
	  FileOutputFormat.setOutputPath(job, output );
	  //提交job
	  job.waitForCompletion(true);
 }
}
