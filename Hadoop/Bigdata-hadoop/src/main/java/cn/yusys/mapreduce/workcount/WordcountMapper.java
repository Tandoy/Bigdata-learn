package cn.yusys.mapreduce.workcount;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * hadoop 利用mapreduce分布式计算框架来统计多个大文件中每个单词出现的次数
 * @author Administrator
 *
 */
/**
 * KEYIN:默认情况下:是mr框架读到的一行内容的起始偏移量   Long 在hadoop 中有比Serializable 更为精简的序列化接口 LongWritable
 * VALUEIN:默认情况下 : 是mr框架读到的一行文本内容 String 同上 Text序列号接口
 * KEYOUT:默认情况下:是mr框架经过分布式计算后输出的key 单词 String
 * VALUEOUT:默认情况下:是mr框架经过分布式计算后输出的value 每个单词出现的次数 Ieteger 同上 IntWritable
 * @author Administrator
 * 输入LongWritable:行号
 * 输入Text:一行内容
 * 输出Text:单词
 * 输出IntWritable:单词个数
 */
public class WordcountMapper extends Mapper<LongWritable, Text, Text, IntWritable>{
   Text k = new Text();
   IntWritable v = new IntWritable(1);
   @Override
   protected void map(LongWritable key, Text value,Context context)
		throws IOException, InterruptedException {
	    // 1.将value转换成字符串
	   String str = value.toString();
	   // 2.利用空格切割
	   String[] words = str.split(" ");
	   // 3.循环放入Context中
	   for (String word : words) {
		   // 单词:1 键值对的形式写入到context中
		   k.set(word);
		   context.write(k,v);
	}
 }
}
