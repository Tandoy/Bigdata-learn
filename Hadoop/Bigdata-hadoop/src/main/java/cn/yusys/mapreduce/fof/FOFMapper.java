package cn.yusys.mapreduce.fof;

import java.io.IOException;

import javax.ws.rs.core.NewCookie;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.StringUtils;

public class FOFMapper extends Mapper<LongWritable, Text, Text, IntWritable>{
	Text mkey = new Text();
	IntWritable mval = new IntWritable();
   @Override
protected void map(LongWritable key, Text value, Context context)
		throws IOException, InterruptedException {
	    String[] strings = StringUtils.split(value.toString(), ' ');
	    for (int i = 1; i < strings.length; i++) {
	    	// 直接好友关系
			mkey.set(getFof(strings[0], strings[1]));
			mval.set(0);
			context.write(mkey, mval);
			for (int j = i + 1; j < strings.length; j++) {
				//间接好友关系
				mkey.set(getFof(strings[i], strings[j]));
				mval.set(1);
				context.write(mkey, mval);
			}
		}
}  
   public static String getFof(String s1,String s2) {
	  if(s1.compareTo(s2) < 0) {
		  return s1 + ":" + s2;
	  }
	  return s2 + ":" + s1;
}
}
