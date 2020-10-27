package cn.yusys.mapreduce.sharedfridens;

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
 * 求共同好友(社交粉丝群体数据分析)<第一步>
 * @author Administrator
 *
 */
public class SharedFriends {
	 /**
	  * A:B,C,D,F,E,O
		B:A,C,E,K
		C:F,A,D,I
		D:A,E,F,L
		E:B,C,D,M,L
		F:A,B,C,D,E,O,M
		G:A,C,D,E,F
		H:A,C,D,E,O
		I:A,O
		J:B,O
		K:A,C,D
		L:D,E,F
		M:E,F,G
		O:A,H,I,J
	  * 入参:文本文件 写出到reduce:<>,<>....
	  * @author Administrator
	  *
	  */
	 static class SharedFriendsMapper extends Mapper<LongWritable, Text, Text, Text>{
		 Text k = new Text();
		 Text v = new Text();
		 @Override
		protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			 // A:B,C,D,F,E,O
			  String line = value.toString();
			  String[] persion_fridens = line.split(":");
			  String persion = persion_fridens[0];
			  String friends = persion_fridens[1];
			  v.set(persion);
			  for (String friend : friends.split(",")) {
				  k.set(friend);
				  context.write(k,v);
			}
		}
	 }
	 static class SharedFriendsReduce extends Reducer<Text, Text, Text, Text>{
		 Text k = new Text();
		 Text v = new Text();
		 // 输入为 多组k-v对<好友,人>.....
		 @Override
		protected void reduce(Text friend, Iterable<Text> persions, Reducer<Text, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			 StringBuilder sb = new StringBuilder();
			  k.set(friend);
			  for (Text persion : persions) {
				  sb.append(persion).append(",");
				  v.set(sb.toString());
			}
			  context.write(k,v);
		}
	 }
     public static void main(String[] args) throws Exception {
    	 Configuration conf = new Configuration();
			Job wcjob = Job.getInstance(conf);
			// 指定我这个job所在的jar包
	        // wcjob.setJar("/home/hadoop/wordcount.jar");
			wcjob.setJarByClass(SharedFriends.class);
			wcjob.setMapperClass(SharedFriendsMapper.class);
			wcjob.setReducerClass(SharedFriendsReduce.class);
			// 设置自动分区算法
			// 设置我们的业务逻辑Mapper类的输出key和value的数据类型
			wcjob.setMapOutputKeyClass(Text.class);
			wcjob.setMapOutputValueClass(Text.class);
			// 设置我们的业务逻辑Reducer类的输出key和value的数据类型
			wcjob.setOutputKeyClass(Text.class);
			wcjob.setOutputValueClass(Text.class);
			// 指定要处理的数据所在的位置
			FileInputFormat.setInputPaths(wcjob, new Path(args[0]));
			// 指定处理完成之后的结果所保存的位置
			FileOutputFormat.setOutputPath(wcjob, new Path(args[1]));
			// 向yarn集群提交这个job
			boolean res = wcjob.waitForCompletion(true);
			System.exit(res?0:1);
	}
}
