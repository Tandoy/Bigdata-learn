package cn.yusys.mapreduce.weather;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * @author Tangzhi mail:tangzhi8023@gmail.com
 * @description:天气案例 Reduce阶段
 * @date 2019年9月1日
 */
public class WeatherReducer extends Reducer<WeatherBean, IntWritable, Text, IntWritable>{
           Text rkey = new Text();  
           IntWritable rval = new IntWritable();
	       @Override
            protected void reduce(WeatherBean key, Iterable<IntWritable> values,Context context)
            		throws IOException, InterruptedException {
            	   //相同的key为一组，调用一次reduce方法
            	int flg = 0;
           		int day = 0;
           		for (IntWritable v : values) {
           			//温度第一高
           			if(flg == 0){
           				day = key.getDay();	
           				rkey.set(key.getYear()+"-"+key.getMonth()+"-"+key.getDay());
           				rval.set(key.getTemperature());
           				context.write(rkey,rval);
           				flg ++;
           				
           			}
           			//温度第二高
           			if(flg != 0 && day != key.getDay()){
           				rkey.set(key.getYear()+"-"+key.getMonth()+"-"+key.getDay());
           				rval.set(key.getTemperature());
           				context.write(rkey,rval);
           				break;
           			}
            	   
            }
     }
}
