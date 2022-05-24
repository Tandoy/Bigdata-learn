package com.flink.udf;

import com.flink.bean.Event;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.FunctionHint;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.types.Row;

public class UDTFTest {
    public static void main(String[] args) throws Exception {
        // 1.创建环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        // 2.env相关设置
        env.setParallelism(1);

        // 3.source
        DataStreamSource<Event> streamSource = env.addSource(new SourceFunction<Event>() {
            @Override
            public void run(SourceContext<Event> sourceContext) throws Exception {
                while (true) {
                    sourceContext.collect(new Event("jack jack",1));
                    sourceContext.collect(new Event("andy tangzhi",0));
                    sourceContext.collect(new Event("tandoy g",0));
                    Thread.sleep(5000);
                }
            }

            @Override
            public void cancel() {

            }
        });

        // stream to table
        tableEnv.registerDataStream("mytable",streamSource);

        // register function
        tableEnv.createTemporarySystemFunction("SplitFunction", SplitFunction.class);

        // select
        Table table = tableEnv.sqlQuery(
                "SELECT name, type,word,length " +
                        "FROM mytable, LATERAL TABLE(SplitFunction(name))");

        tableEnv.toRetractStream(table, Row.class).print();

        // execute
        env.execute("UDTFTest");
    }

    @FunctionHint(output = @DataTypeHint("ROW<word STRING, length INT>")) // UDTF函数必须声明output dataType
    public static class SplitFunction extends TableFunction {
        public void eval(String str) {
            for (String s : str.split(" ")) {
                // use collect(...) to emit a row
                collect(Row.of(s, s.length()));
            }
        }
    }
}
