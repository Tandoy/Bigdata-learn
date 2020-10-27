package cn.yusys.hive;

import java.util.HashMap;

import org.apache.hadoop.hive.ql.exec.UDF;

/**
 * @author Tangzhi mail:tangzhi8023@gmail.com
 * @description:hive自定义函数
 * @date 2018年10月21日
 */
public class ToUpper extends UDF{
	public static HashMap<String, String> proviceMap = new HashMap<String, String>();
	static {
		proviceMap.put("136", "hunan");
		proviceMap.put("137", "beijing");
		proviceMap.put("138", "guangzhou");
	}
     /**
      * 实现hive数据小写转大写
      */
	public String evaluate(String s){
		String result = s.toUpperCase();
		return result;
	}
	/**
	 * 实现手机号与归属地的联系
	 */
	public String evaluat(int phonenbr) {
		    String phone = String.valueOf(phonenbr);
	        return proviceMap.get(phone.substring(0, 3)) == null?"chawucidi":proviceMap.get(phone.substring(0,3));
	}
}
