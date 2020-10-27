package cn.yusys.mapreduce.workcount;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * hadoop 利用mapreduce分布式计算框架来统计多个文件中每个单词出现的次数
 * @author Administrator
 *
 */
/**
 * KEYIN VALUEIN : 对应map的context的输出Text IntWritable
 * KEYOUT, VALUEOUT : 单词 以及出现次数 Text IntWritable
 * @author Administrator
 *
 */
public class WordcountReducer extends Reducer<Text, IntWritable, Text, IntWritable>{
	@Override
	protected void reduce(Text key, Iterable<IntWritable> values,
			Context context) throws IOException, InterruptedException {
		/**
		 * key 是一组相同单词  values 多个次数迭代 <kobe,1> <kobe,1> <kobe,1>.....
		 */
		// 统计一组单词的出现次数
		int count = 0;
		Iterator<IntWritable> iterator = values.iterator();
		while(iterator.hasNext()){
			count += iterator.next().get();
		}
		/*for (IntWritable value : values) {
			count += value.get();
		}*/
		// mr框架写出
		context.write(key, new IntWritable(count));
	}
	
}
