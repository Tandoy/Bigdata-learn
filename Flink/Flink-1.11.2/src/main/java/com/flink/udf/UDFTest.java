package com.flink.udf;

import com.flink.bean.Event;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.types.Row;

public class UDFTest {
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
                    sourceContext.collect(new Event("jack",1));
                    sourceContext.collect(new Event("andy",0));
                    sourceContext.collect(new Event("tandoy",0));
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
        tableEnv.createTemporarySystemFunction("SubstringFunction", SubstringFunction.class);

        // select
        Table table = tableEnv.sqlQuery("SELECT SubstringFunction(name, 0, 3) FROM mytable");

        tableEnv.toRetractStream(table, Row.class).print();

        // execute
        env.execute("UDFTest");
    }

    public static class SubstringFunction extends ScalarFunction {
        public String eval(String s, Integer begin, Integer end) {
            return s.substring(begin, end);
        }
    }
}
