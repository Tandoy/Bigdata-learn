package cn.yusys.mapreduce.fluwcount;

import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

/**
 * 根据省份分区分布式统计用户手机号
 * @author Administrator
 *
 */
public class ProvincePartitioner extends Partitioner<Text, FluwBean>{
	/**
	 * 模拟手机号省份分区数据
	 */
	public static Map<String,Integer> provinceDict = new HashMap<String,Integer>();
	static{
		provinceDict.put("135", 0);
		provinceDict.put("136", 1);
		provinceDict.put("137", 2);
		provinceDict.put("139", 3);
	}
	@Override
	public int getPartition(Text key, FluwBean value, int numPartitioners) {
		String prefix = key.toString().substring(0, 3);
		Integer provinceId = provinceDict.get(prefix);
		return provinceId == null?4:provinceId;
	}

}
