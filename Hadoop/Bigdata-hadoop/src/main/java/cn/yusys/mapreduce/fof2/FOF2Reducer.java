package cn.yusys.mapreduce.fof2;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

public class FOF2Reducer extends Reducer<FOF2Bean, IntWritable, FOF2Bean, IntWritable>{
    @Override
    protected void reduce(FOF2Bean key, Iterable<IntWritable> values,Context context)
     		throws IOException, InterruptedException {
    	/**
    	 * cat:hello	1
    	 * cat:hello	2
    	 * cat:hadoop	2
    	 * cat:tom	3
    	 */
    	int top = 0;
    	for (IntWritable value : values) {
			if (!value.equals(null)) {
				if (top <= 5) {
					context.write(key,value);
				}
			}
		}
    }
}
