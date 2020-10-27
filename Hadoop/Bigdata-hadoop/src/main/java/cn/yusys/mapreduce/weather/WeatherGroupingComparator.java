package cn.yusys.mapreduce.weather;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

/**
 * 
 * @author Tangzhi mail:tangzhi8023@gmail.com
 * @description:Reducer阶段的分组排序算法，若不重写，框架默认会采用Map阶段的排序算法
 * @date 2019年9月1日
 */
public class WeatherGroupingComparator extends WritableComparator{
          public WeatherGroupingComparator() {
        	  super(WeatherBean.class,true);
		}
          @Override
        public int compare(WritableComparable a, WritableComparable b) {
        	  WeatherBean w1 = (WeatherBean) a;
              WeatherBean w2 = (WeatherBean) b;
              // 进行年、月比较
              int res1 = Integer.compare(w1.getYear(), w2.getYear());
              if (res1 == 0) {
				 return Integer.compare(w1.getMonth(), w2.getMonth());
			}
              return res1;
        }
}
