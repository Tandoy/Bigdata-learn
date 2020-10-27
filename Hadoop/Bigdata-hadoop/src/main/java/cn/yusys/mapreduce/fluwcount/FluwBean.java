package cn.yusys.mapreduce.fluwcount;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

/**
 * 用户上行流量 下行流量 实体类(实现hadoop序列化接口、根据总流量大小排序)
 * @author Administrator
 *
 */
public class FluwBean implements WritableComparable<FluwBean>{
    long upFluw; // 上行流量
    long downFluw; // 下行流量
    long sumFluw; // 总流量
  //反序列化时，需要反射调用空参构造函数，所以要显示定义一个
  	public FluwBean(){}
    public FluwBean(long upFluw, long downFluw) {
		super();
		this.upFluw = upFluw;
		this.downFluw = downFluw;
		this.sumFluw = upFluw + downFluw;
	}
    public void set(long upFluw,long downFluw){
    	this.upFluw = upFluw;
    	this.downFluw = downFluw;
    	this.sumFluw = upFluw +downFluw;
    }
	public long getUpFluw() {
		return upFluw;
	}
	public void setUpFluw(long upFluw) {
		this.upFluw = upFluw;
	}
	public long getDownFluw() {
		return downFluw;
	}
	public void setDownFluw(long downFluw) {
		this.downFluw = downFluw;
	}	
	public long getSumFluw() {
		return sumFluw;
	}
	public void setSumFluw(long sumFluw) {
		this.sumFluw = sumFluw;
	}
	/**
	 * 自定义序列化对象
	 * 序列化
	 */
	public void write(DataOutput out) throws IOException {
		    out.writeLong(upFluw);
		    out.writeLong(downFluw);
		    out.writeLong(sumFluw);
	}
	/**
	 * 反序列化
	 * 注意:hadoop序列化与反序列化顺序应一致
	 */
	public void readFields(DataInput in) throws IOException {
		    upFluw = in.readLong();
		    downFluw = in.readLong();
		    sumFluw = in.readLong();
	}
	/**
	 * 重写toString方法
	 */
	@Override
	public String toString() {
		return  upFluw + "\t" + downFluw + "\t" + sumFluw;
	}
	// 自定义排序接口
	public int compareTo(FluwBean o) {
		return this.getSumFluw() > o.getSumFluw()?-1:1;
	}
}
