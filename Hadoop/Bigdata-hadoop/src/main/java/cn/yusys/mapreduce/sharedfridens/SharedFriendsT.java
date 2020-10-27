package cn.yusys.mapreduce.sharedfridens;
import java.io.IOException;
import java.util.Arrays;

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
 * 求共同好友(社交粉丝群体数据分析)<第二步>
 * @author Administrator
 */
public class SharedFriendsT {
	/**
	A       I,K,C,B,G,F,H,O,D,
	B       A,F,J,E,
	C       A,E,B,H,F,G,K,
	D       G,C,K,A,L,F,E,H,
	E       G,M,L,H,A,F,B,D,
	F       L,M,D,C,G,A,
	G       M,
	H       O,
	I       O,C,
	J       O,
	K       B,
	L       D,E,
	M       E,F,
	O       A,H,I,J,F,
	 * @author Administrator
	 *
	 */
    static class SharedFriendsTMapper extends Mapper<LongWritable, Text, Text, Text>{
    	@Override
    	protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, Text>.Context context)
    			throws IOException, InterruptedException {
    		String line = value.toString();
    		String[] friend_persions = line.split("\t");
    		String[] persions = friend_persions[1].split(",");
    		String friend = friend_persions[0];
    		// 对人进行排序
    		Arrays.sort(persions);
    		for(int i = 0;i<persions.length - 2;i++){
    		    for(int j = i + 1;j<persions.length - 1;j++){
    		    	context.write(new Text(persions[i] + "-" +persions[j]), new Text(friend));
    		    }	
    		}
    	}
    }
    static class SharedFriendsTReduce extends Reducer<Text, Text, Text, Text>{
    	 @Override
    	protected void reduce(Text persion_persion, Iterable<Text> friends, Reducer<Text, Text, Text, Text>.Context context)
    			throws IOException, InterruptedException {
    		 StringBuilder sb = new StringBuilder();
    		 for (Text friend : friends) {
				  sb.append(friend).append(",");
			}
    		 context.write(new Text(persion_persion), new Text(sb.toString()));
    	}
    }
    static class SharedFriendsTworeduce extends Reducer<Text, Text, Text, Text>{
    	@Override
    	protected void reduce(Text persion_persion, Iterable<Text> friends, Reducer<Text, Text, Text, Text>.Context arg2)
    			throws IOException, InterruptedException {
    		
    	}
    	
    }
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job wcjob = Job.getInstance(conf);
		// 指定我这个job所在的jar包
        // wcjob.setJar("/home/hadoop/wordcount.jar");
		wcjob.setJarByClass(SharedFriendsT.class);
		wcjob.setMapperClass(SharedFriendsTMapper.class);
		wcjob.setReducerClass(SharedFriendsTReduce.class);
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
