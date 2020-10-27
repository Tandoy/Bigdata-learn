package cn.yusys.mapreduce.weather;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Partitioner;
/**
 * 
 * @author Tangzhi mail:tangzhi8023@gmail.com
 * @description: map阶段分区
 * @date 2019年9月1日
 */
public class WeatherPartition extends Partitioner<WeatherBean,IntWritable>{
	@Override
	public int getPartition(WeatherBean key, IntWritable value, int numPartitioners) {
		return key.getYear() % numPartitioners;
	}

}
