package cn.yusys.mapreduce.fof2;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Partitioner;

public class FOF2Partition extends Partitioner<FOF2Bean, IntWritable>{

	@Override
	public int getPartition(FOF2Bean key, IntWritable value, int numPartitioners) {
		return key.getRecommen() % numPartitioners;
	}

}
