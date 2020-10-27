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
 * 根据已经分布式计算的用户总流量进行排序
 * @author Administrator
 *
 */
public class FluwCountSort {
	/**
	 * Text : 13502468823     7335    110349  117684
	 * FluwBean : 总流量
	 * Text : 手机号
	 * @author Administrator
	 */
    static class FluwCountSortMappper extends Mapper<LongWritable, Text, FluwBean, Text>{
    	FluwBean bean = new FluwBean();
    	Text v = new Text();
        @Override
    	protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, FluwBean, Text>.Context context)
    			throws IOException, InterruptedException {
    		String line = value.toString();
    		String[] fields = line.split("\t");
    		// 手机号
    		String phoneNbr = fields[0];
    		// 上行流量 下行流量
    		long upFluw = Long.parseLong(fields[1]);
    		long downFluw = Long.parseLong(fields[2]);
    		bean.set(upFluw, downFluw);
    		v.set(phoneNbr);
    		// hadoop mr框架根据key自动排序(需实现排序接口)
    		context.write(bean, v);
    	}
    }
    
    /**
     * 入参 : 流量实体类 手机号
     * 写出 : 手机号 流量实体类 
     * @author Administrator
     *
     */
    static class FluwCountSortReduce extends Reducer<FluwBean, Text, Text, FluwBean>{
    	  @Override
    	protected void reduce(FluwBean bean, Iterable<Text> values, Reducer<FluwBean, Text, Text, FluwBean>.Context context)
    			throws IOException, InterruptedException {
    		  // 迭代以手机号为key 流量bean为value 输出
    		 context.write(values.iterator().next(), bean);
    	}
    }
    /**
     * mr框架驱动类
     * @param args
     * @throws IOException 
     * @throws InterruptedException 
     * @throws ClassNotFoundException 
     */
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		Configuration conf = new Configuration();
		Job wcjob = Job.getInstance(conf);
		//指定我这个job所在的jar包
//		wcjob.setJar("/home/hadoop/wordcount.jar");
		wcjob.setJarByClass(FluwCountSort.class);
		
		wcjob.setMapperClass(FluwCountSortMappper.class);
		wcjob.setReducerClass(FluwCountSortReduce.class);
		//设置我们的业务逻辑Mapper类的输出key和value的数据类型
		wcjob.setMapOutputKeyClass(FluwBean.class);
		wcjob.setMapOutputValueClass(Text.class);
		//设置我们的业务逻辑Reducer类的输出key和value的数据类型
		wcjob.setOutputKeyClass(Text.class);
		wcjob.setOutputValueClass(FluwBean.class);
		//指定要处理的数据所在的位置
		FileInputFormat.setInputPaths(wcjob, new Path(args[0]));
		//指定处理完成之后的结果所保存的位置
		FileOutputFormat.setOutputPath(wcjob, new Path(args[1]));
		
		//向yarn集群提交这个job
		boolean res = wcjob.waitForCompletion(true);
		System.exit(res?0:1);
	}

}
