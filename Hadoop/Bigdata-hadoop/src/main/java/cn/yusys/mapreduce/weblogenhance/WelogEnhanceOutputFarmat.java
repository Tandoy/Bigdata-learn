package cn.yusys.mapreduce.weblogenhance;

import java.io.IOException;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * 自定义hadoop mr框架输出数据位置
 * @author Administrator
 *
 */
public class WelogEnhanceOutputFarmat extends FileOutputFormat<Text, NullWritable>{

	@Override
	public RecordWriter<Text, NullWritable> getRecordWriter(TaskAttemptContext context)
			throws IOException, InterruptedException {
		FileSystem fs = FileSystem.get(context.getConfiguration());
		Path enhancePath = new Path("hdfs://192.168.25.132/weblogenhance/url.data");
		Path tocrowlPath = new Path("hdfs://192.168.25.132/weblogenhance/tocrowl.data");
		FSDataOutputStream enhance = fs.create(enhancePath);
		FSDataOutputStream tocrowl = fs.create(tocrowlPath);
		return new WeblogEnhanceRecordWriter(enhance, tocrowl);
	}
    static class  WeblogEnhanceRecordWriter extends RecordWriter<Text, NullWritable>{
    	FSDataOutputStream enhance = null;
    	FSDataOutputStream tocrowl = null;
		public WeblogEnhanceRecordWriter(FSDataOutputStream enhance, FSDataOutputStream tocrowl) {
			super();
			this.enhance = enhance;
			this.tocrowl = tocrowl;
		}
		
		@Override
		public void write(Text key, NullWritable v) throws IOException, InterruptedException {
			String line = key.toString();
			if(line.contains("tocrowl")){
				// 写入至待爬取清单
				tocrowl.write(line.getBytes());
			}else {
				// 进行日志增强
				enhance.write(line.getBytes());
			}
		}
		@Override
		public void close(TaskAttemptContext context) throws IOException, InterruptedException {
			if(enhance != null){
				enhance.close();
			} 
			if(tocrowl != null){
				tocrowl.close();
			}
		}
    	
    }
    
}
