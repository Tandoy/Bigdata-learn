package cn.yusys.mapreduce.inverindex;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
/**
 * 倒排索引实现
 * @author Administrator
 *
 */
public class Inverindex {
    static class InverindexMapper extends Mapper<LongWritable, Text, Text, IntWritable>{
    	Text k = new Text();
    	IntWritable v = new IntWritable(1);
    	@Override
    	protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, IntWritable>.Context context)
    			throws IOException, InterruptedException {
    		  // 文本截取
    		String line = value.toString();
    		String[] worlds = line.split(" ");
    		 // 得到文件名
    		 FileSplit inputSplit = (FileSplit) context.getInputSplit();
    		 String name = inputSplit.getPath().getName();
    		 // 写出到reduce处理
    		for (String world : worlds) {
				 k.set(world + "--" + name);
				 context.write(k, v);
			}
    	}
    }
    static class InverindexReduce extends Reducer<Text, IntWritable, Text, IntWritable>{
    	  @Override
    	protected void reduce(Text key, Iterable<IntWritable> values,
    			Reducer<Text, IntWritable, Text, IntWritable>.Context context) throws IOException, InterruptedException {
    		  // 进行分布式计算
    		  int count = 0;
    		  for (IntWritable value : values) {
				  count += value.get();
			}
    		  context.write(key, new IntWritable(count));
    	}
    }
	public static void main(String[] args) throws Exception {
		   Configuration conf = new Configuration();
			Job wcjob = Job.getInstance(conf);
			// 指定我这个job所在的jar包
	        // wcjob.setJar("/home/hadoop/wordcount.jar");
			wcjob.setJarByClass(Inverindex.class);
			wcjob.setMapperClass(InverindexMapper.class);
			wcjob.setReducerClass(InverindexReduce.class);
			// 设置自动分区算法
			// 设置我们的业务逻辑Mapper类的输出key和value的数据类型
			wcjob.setMapOutputKeyClass(Text.class);
			wcjob.setMapOutputValueClass(IntWritable.class);
			// 设置我们的业务逻辑Reducer类的输出key和value的数据类型
			wcjob.setOutputKeyClass(Text.class);
			wcjob.setOutputValueClass(IntWritable.class);
			// 指定要处理的数据所在的位置
			FileInputFormat.setInputPaths(wcjob, new Path(args[0]));
			// 指定处理完成之后的结果所保存的位置
			FileOutputFormat.setOutputPath(wcjob, new Path(args[1]));
			// 向yarn集群提交这个job
			boolean res = wcjob.waitForCompletion(true);
			System.exit(res?0:1);
	}

}
