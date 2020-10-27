package cn.yusys.mapreduce.fof2;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import cn.yusys.mapreduce.fof.FOFMapper;

/**
 * 根据上次MR结果进行好友推荐优化
 * @author Tangzhi mail:tangzhi8023@gmail.com
 * @description: cat:hello	1
			     cat:mr	1
				 cat:world	1
				 hadoop:hello	2
				 hadoop:hive	1
				 hadoop:mr	1
				 hello:hive	2
				 hello:mr	1
				 hello:world	1
				 hive:tom	3
				 hive:world	2
 * @date 2019年9月8日
 */
public class FOF2Runner {
   public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
	Configuration conf = new Configuration(true);
	Job job = Job.getInstance(conf);
	//conf:采用默认TextInputFormat
	//map:
	job.setMapperClass(FOF2Mapper.class);
	job.setMapOutputKeyClass(FOF2Bean.class); 
	job.setMapOutputValueClass(IntWritable.class);
	job.setPartitionerClass(FOF2Partition.class); //<k,v> --> <k,v,p>
	//job.setSortComparatorClass(FOF2SortComparator.class);//环形缓冲区排序
	//reduce:
	//reduce阶段排序同样采用Map排序方式
	job.setReducerClass(FOF2Reducer.class);
	FileInputFormat.setInputPaths(job, new Path(args[0]));
	// 指定job的输出结果所在目录
	FileOutputFormat.setOutputPath(job, new Path(args[1]));
	job.waitForCompletion(true);
}
}
