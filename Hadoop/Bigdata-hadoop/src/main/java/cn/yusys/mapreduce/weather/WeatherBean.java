package cn.yusys.mapreduce.weather;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

/**
 * 天气实例对象
 * @author Tangzhi mail:tangzhi8023@gmail.com
 * @description:
 * @date 2019年9月1日
 */
public class WeatherBean implements WritableComparable<WeatherBean>{
    private int year;
    private int month;
    private int day;
    private int temperature;
	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getTemperature() {
		return temperature;
	}

	public void setTemperature(int temperature) {
		this.temperature = temperature;
	}

	public void write(DataOutput out) throws IOException {
			out.writeInt(year);
			out.writeInt(month);
			out.writeInt(day);
			out.writeInt(temperature);
	}

	public void readFields(DataInput in) throws IOException {
		// 反序列化必须与序列化顺序相同
		this.year = in.readInt();
		this.month = in.readInt();
		this.day = in.readInt();
		this.temperature = in.readInt();
		
	}

	public int compareTo(WeatherBean that) {
		//按日期正序排序
		int res1 = Integer.compare(this.getYear(), that.getYear());
		if (res1 == 0) {
			int res2 = Integer.compare(this.getMonth(), that.getMonth());
			if (res2 == 0) {
				return Integer.compare(this.getDay(), that.getDay());
			}
			return res2;
		}
		return res1;
	}
}
