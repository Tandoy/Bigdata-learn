package cn.yusys.mapreduce.fof2;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.StringUtils;

public class FOF2Mapper extends Mapper<LongWritable, Text, FOF2Bean, IntWritable>{
	FOF2Bean mkey1 = new FOF2Bean();
	FOF2Bean mkey2 = new FOF2Bean();
	IntWritable mval = new IntWritable();
   @Override
protected void map(LongWritable key, Text value, Context context)
		throws IOException, InterruptedException {
	     /**
	      * cat:hello	1
	        cat:mr	1
		    cat:world	1
		    hadoop:hello	2
	      */
	     String[] str = StringUtils.split(value.toString(),'\t');
	     mkey1.setFrid1(str[0].split(":")[0].toString());
	     mkey1.setFrid2(str[0].split(":")[1].toString());
	     mkey1.setRecommen(Integer.parseInt(str[1]));
	     mval.set(Integer.parseInt(str[1]));
	     context.write(mkey1, mval);
	     
	     mkey2.setFrid1(str[0].split(":")[1].toString());
	     mkey2.setFrid1(str[0].split(":")[0].toString());
	     mkey2.setRecommen(Integer.parseInt(str[1]));
	     context.write(mkey2, mval);
}
}
