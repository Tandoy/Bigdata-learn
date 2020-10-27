package cn.yusys.mapreduce.fof2;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

public class FOF2Bean implements WritableComparable<FOF2Bean>{
    private String frid1;
    private String frid2;
    private int recommen;
    
	public FOF2Bean() {
	}

	public FOF2Bean(String frid1, String frid2, int recommen) {
		super();
		this.frid1 = frid1;
		this.frid2 = frid2;
		this.recommen = recommen;
	}

	public String getFrid1() {
		return frid1;
	}

	public void setFrid1(String frid1) {
		this.frid1 = frid1;
	}

	public String getFrid2() {
		return frid2;
	}

	public void setFrid2(String frid2) {
		this.frid2 = frid2;
	}

	public int getRecommen() {
		return recommen;
	}

	public void setRecommen(int recommen) {
		this.recommen = recommen;
	}

	public void write(DataOutput out) throws IOException {
                out.writeBytes(frid1);
                out.writeBytes(frid2);
                out.write(recommen);
	}

	public void readFields(DataInput in) throws IOException {
		//反序列化必须与序列化顺序相同
                this.frid1 = in.readUTF();
                this.frid2 = in.readUTF();
                this.recommen = in.readInt();
	}

	public int compareTo(FOF2Bean that) {
		//根据推荐度进行比较TopN
		return Integer.compare(this.getRecommen(),that.getRecommen());
	}
   
}
