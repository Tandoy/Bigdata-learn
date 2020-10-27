package cn.yusys.mapreduce.fof;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class FOFReducer extends Reducer<Text, IntWritable, Text, IntWritable>{
	IntWritable rval = new IntWritable();
   @Override
protected void reduce(Text key, Iterable<IntWritable> values,
		Context context) throws IOException, InterruptedException {
	   //hadoop : tom 0
	   //hadoop : tom 1
	   //hadoop : tom 0
	   int flag = 0;
	   int sum = 0;
	   for (IntWritable v : values) {
		   //数据为直接好友
		  if (v.get() == 0) {
			flag = 1;
		}
		  //间接好友
		  sum += v.get();
	}
	    if (flag == 0) {
	    	rval.set(sum);
			context.write(key, rval);
		}
}
}
