package cn.yusys.mapreduce.weather;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.StringUtils;

/**
 * 
 * @author Tangzhi mail:tangzhi8023@gmail.com
 * @description:Mapper
 * @date 2019年9月1日
 */
//默认为TextInputFormat
public class WeatherMapper extends Mapper<LongWritable, Text, WeatherBean, IntWritable>{
		WeatherBean Weather = new WeatherBean();
		IntWritable mvalue = new IntWritable();
           //数据样例:  1949-10-01 14:21:02	34c
	       @Override
	    protected void map(LongWritable key, Text value,Context context)
	    		throws IOException, InterruptedException {
	    	    String[] flieds = StringUtils.split(value.toString(),'\t');
	    	    SimpleDateFormat  sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date date = null;
				try {
					date = sdf.parse(flieds[0]);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				Weather.setYear(cal.get(Calendar.YEAR));
				Weather.setMonth(cal.get(Calendar.MONTH)+1);
				Weather.setDay(cal.get(Calendar.DAY_OF_MONTH));
				int temperature = Integer.parseInt(flieds[1].substring(0, flieds[1].length()-1));
				Weather.setTemperature(temperature);
				mvalue.set(temperature);
				context.write(Weather,mvalue);
	    }
}
