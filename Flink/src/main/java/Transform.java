import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.operators.FilterOperator;
import org.apache.flink.api.java.operators.FlatMapOperator;
import org.apache.flink.api.java.operators.MapOperator;
import org.apache.flink.util.Collector;

public class Transform {
    public static void main(String[] args) throws Exception {
        // 转换算子
        ExecutionEnvironment executionEnvironment = ExecutionEnvironment.getExecutionEnvironment();
        // 1.map: 将输入字符串转换成字符串长度
        DataSource<String> dataSource = executionEnvironment.readTextFile("D:\\Downloads\\github\\Bigdata-learn\\Flink\\src\\main\\resources\\test.txt");
        MapOperator<String, Integer> mapOperator = dataSource.map(new MapFunction<String, Integer>() {
            public Integer map(String s) throws Exception {
                return s.length();
            }
        });
        // 2.flatMap 将一行数据分开
        FlatMapOperator<String, String> flatMapOperator = dataSource.flatMap(new FlatMapFunction<String, String>() {
            public void flatMap(String s, Collector<String> collector) throws Exception {
                String[] fileds = s.split(" ");
                for(String filed:fileds){
                    collector.collect(filed);
                }
            }
        });
        // 3.Filter 过滤
        FilterOperator<String> filterOperator = dataSource.filter(new FilterFunction<String>() {
            public boolean filter(String s) throws Exception {
                return s.startsWith("hello");
            }
        });
        mapOperator.print();
        flatMapOperator.print();
        filterOperator.print();
        executionEnvironment.execute();
    }
}
