package cn.yusys.mapreduce.weather;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
/**
 * 
 * @author Tangzhi mail:tangzhi8023@gmail.com
 * @description: map阶段 <k,v,p>进入环形缓冲区中排序算法
 * @date 2019年9月1日
 */
public class WeatherSortComparator extends WritableComparator{
        public WeatherSortComparator() {
               super(WeatherBean.class,true);
 		}
        @Override
        public int compare(WritableComparable a, WritableComparable b) {
               WeatherBean w1 = (WeatherBean) a;
               WeatherBean w2 = (WeatherBean) b;
               // 按年、月正序；温度倒序进行排序
               int res1 = Integer.compare(w1.getYear(), w2.getYear());
               if (res1 == 0) {
				 int res2 = Integer.compare(w1.getMonth(), w2.getMonth());
				 if (res2 == 0) {
					 return -Integer.compare(w1.getTemperature(), w2.getTemperature());
				}
				 return res2;
			}
               return res1;
        }
}
