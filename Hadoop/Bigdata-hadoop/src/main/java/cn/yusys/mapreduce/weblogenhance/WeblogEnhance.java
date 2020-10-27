package cn.yusys.mapreduce.weblogenhance;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;



/**
 * 分析用户访问地址规则  若url已存在规则库则增强当前用户日志 若没有则解析失败存在规则库中
 * @author Administrator
 *
 */
public class WeblogEnhance {
    static class WeblogEnhanceMapper extends Mapper<LongWritable, Text, Text, NullWritable>{
    	Map<String,String> ruleMap = new HashMap<String, String>();
    	Text k = new Text();
    	NullWritable v = NullWritable.get();
    	// 初始化规则库(sql查询)
    	@Override
    	protected void setup(Mapper<LongWritable, Text, Text, NullWritable>.Context context)
    			throws IOException, InterruptedException {
    		    try {
					DBLoader.dbLoader(ruleMap);
				} catch (Exception e) {
					e.printStackTrace();
				}
    	}
    	
    	@Override
    	protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, NullWritable>.Context context)
    			throws IOException, InterruptedException {
    		  String line = value.toString();
    		  String[] fields = line.split("\t");
    		  try {
    			  String url = fields[26];
        		  String content_tag = ruleMap.get(url);
        		  if(content_tag == null){
        			  // 规则库没有此url记录 写至待爬取清单
        			  k.set(url + "\t" + "tocrowl" +"\n"); 
        			  context.write(k, v);
        		  }else {
        			  // 规则库中有此url记录 进行日志增强
        			  k.set(line + "\t" + content_tag + "\n");
        			  context.write(k, v);
        		  }
			} catch (Exception e) {
				  e.printStackTrace();
			}
    	}
    }
	public static void main(String[] args) throws Exception {
		 Configuration conf = new Configuration();
			Job wcjob = Job.getInstance(conf);
			// 指定我这个job所在的jar包
	        // wcjob.setJar("/home/hadoop/wordcount.jar");
			wcjob.setJarByClass(WeblogEnhance.class);
			wcjob.setMapperClass(WeblogEnhanceMapper.class);
			// 设置自动分区算法
			// 设置我们的业务逻辑Mapper类的输出key和value的数据类型
			wcjob.setMapOutputKeyClass(Text.class);
			wcjob.setMapOutputValueClass(NullWritable.class);
			// 自定义OutPutFarmat
			wcjob.setOutputFormatClass(WelogEnhanceOutputFarmat.class);
			// 设置我们的业务逻辑Reducer类的输出key和value的数据类型
			// 指定要处理的数据所在的位置
			FileInputFormat.setInputPaths(wcjob, new Path(args[0]));
			// 指定处理完成之后的结果所保存的位置
		    FileOutputFormat.setOutputPath(wcjob, new Path(args[1]));
			// 向yarn集群提交这个job
			boolean res = wcjob.waitForCompletion(true);
			System.exit(res?0:1);
	}

}
