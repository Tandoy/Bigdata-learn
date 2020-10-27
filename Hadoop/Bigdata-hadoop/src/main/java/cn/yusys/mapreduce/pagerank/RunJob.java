package cn.yusys.mapreduce.pagerank;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * 计算网页权重MR
 * @author Tangzhi mail:tangzhi8023@gmail.com
 * @description:
 * @date 2019年9月22日
 */
public class RunJob {
      public static enum MyCounter {
    	  //自定义计数器
    	  my
      }
      public static void main(String[] args) {
		Configuration conf = new Configuration(true);
		conf.set("mapreduce.app-submission.corss-paltform", "true");
		//如果分布式运行,必须打jar包
		//且,client在集群外非hadoop jar 这种方式启动,client中必须配置jar的位置
		conf.set("mapreduce.framework.name", "local");
		//这个配置,只属于,切换分布式到本地单进程模拟运行的配置
		//这种方式不是分布式,所以不用打jar包
		
		double d = 0.1;
		int i = 0;//MR迭代次数统计
		while (true) {
			i++;
			try {
				conf.setInt("runCount", i); //将MR迭代次数作为参数传入分布式计算程序中
				FileSystem fs = FileSystem.get(conf);
				Job job = Job.getInstance(conf);
				job.setJarByClass(RunJob.class);
				job.setJobName("pr" + i);
				job.setMapperClass(PageRankMapper.class);
				job.setReducerClass(PageRankReducer.class);
				job.setMapOutputKeyClass(Text.class);
				job.setMapOutputValueClass(Text.class);
				//初始化输入格式化类 此格式化类将传入数据根据制表符进行切割"\t"
				job.setInputFormatClass(KeyValueTextInputFormat.class);
				Path inputPath = new Path("/data/pagerank/input/");
				if (i > 1) {
					 inputPath = new Path("/data/pagerank/output/pr" + (i -1));
				}
				//设置数据输入路径
				FileInputFormat.addInputPath(job, inputPath );
				Path outputPath = new Path("/data/pagerank/output/pr" + i);			
				//设置数据输出路径
				FileOutputFormat.setOutputPath(job, outputPath);
				if (fs.exists(outputPath)) {
					fs.delete(outputPath, true);
				}
				//提交作业
				boolean completion = job.waitForCompletion(true);
				if (completion) {
					System.out.println("success...");
					//从计数器中拿出所有页面的差值总和
					long sum = job.getCounters().findCounter(MyCounter.my).getValue();
					System.out.println(sum);
					double avgd = sum / 4000.0;
					if (avgd < d) {
						//当多次MR迭代票面值小于目标值时停止计算
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
      
     static class PageRankMapper extends Mapper<Text, Text, Text, Text> {
    	   @Override
    	protected void map(Text key, Text value,Context context)
    			throws IOException, InterruptedException {
    		 //1.得到MR迭代次数
    		   int runCount = context.getConfiguration().getInt("runCount", 1);
    		 //A	   B D
   			 //K:A
   			 //V:B D
    		 //A	0.3 B D 
   			 //K:A
   			 //V:0.3 B D
    		 //2.输入格式化类切割后的key
    		 String page = key.toString();
    		 Node node  = null;
    		 if (runCount == 1) {
    			//3.1 若为第一次迭代，初始化key的票面值
				node = Node.fromMR("1.0", value.toString());
			}else {
				//3.2 若不为第一次迭代，直接输出页面关系即可
			}
    		// A:1.0 B D  传递老的pr值和对应的页面关系
    		 context.write(new Text(page), new Text(node.toString()));
    		//4.计算相对应页面所得到的票面值
    		 if (node.containsAdjacentNodes()) {
				//4.1 计算票面值  老PR值 / 所链接页面个数
    			 double outvalue = node.getPageRank() / node.getAdjacentNodeNames().length;
    			//4.2 对此key所有投票的页面进行逐个赋值
    			 for (int i = 0; i<node.getAdjacentNodeNames().length; i++) {
    				 String outPage = node.getAdjacentNodeNames()[i];
    				 // B:0.5 页面A投给谁，谁作为key，val是票面值
    				 context.write(new Text(outPage), new Text(outvalue + ""));
    			 }
			}
    	}
     }
     
     static class PageRankReducer extends Reducer<Text, Text, Text, Text> {
    	  @Override
    	protected void reduce(Text key, Iterable<Text> iterable, Context context)
    			throws IOException, InterruptedException {
    		//相同的Key为一组，调用一次Reduce方法
    		/**
    		 * key：页面名称比如B
    		 * 包含两类数据
    		 * B:1.0 C  页面对应关系及老的pr值
    		 * B:0.5          投票值
    		 */
    		double sum = 0.0;
  			
  			Node sourceNode = null;
  			for (Text i : iterable) {
  				Node node = Node.fromMR(i.toString());
  				if (node.containsAdjacentNodes()) {
  				  //数据形式为：B:1.0 C
  					sourceNode = node;
  				} else {
  				 //数据形式为：B:0.5
  					sum = sum + node.getPageRank();
  				}
  			}

  			// google：pagerank公式 其中4为页面总数
  			double newPR = (0.15 / 4.0) + (0.85 * sum);
  			System.out.println("*********** new pageRank value is " + newPR);
  			// 把新的pr值和计算之前的pr比较
  			double d = newPR - sourceNode.getPageRank();
  			int j = (int) (d * 1000.0);
  			j = Math.abs(j);
  			System.out.println(j + "___________");
  			context.getCounter(MyCounter.my).increment(j);
  			sourceNode.setPageRank(newPR);
  			context.write(key, new Text(sourceNode.toString()));   
    	}
     }
}
