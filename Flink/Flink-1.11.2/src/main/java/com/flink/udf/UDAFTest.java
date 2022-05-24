package com.flink.udf;

import com.flink.bean.Event;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.functions.AggregateFunction;
import org.apache.flink.types.Row;

public class UDAFTest {
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
                    sourceContext.collect(new Event("jack",120));
                    sourceContext.collect(new Event("jack",100));
                    sourceContext.collect(new Event("tandoy",125));
                    sourceContext.collect(new Event("tandoy",130));
                    Thread.sleep(5000);
                }
            }

            @Override
            public void cancel() {

            }
        });

        // stream to table
        tableEnv.registerDataStream("MyTable",streamSource);

        // register function
        tableEnv.createTemporarySystemFunction("WeightedAvg", WeightedAvg.class);

        // select
        Table table = tableEnv.sqlQuery(
                "SELECT name, WeightedAvg(type) FROM MyTable GROUP BY name");

        tableEnv.toRetractStream(table, Row.class).print();

        // execute
        env.execute("UDAFTest");
    }

    public static class WeightedAvg extends AggregateFunction<Long,WeightedAvgAccumulator> {
        @Override
        public WeightedAvgAccumulator createAccumulator() {
            return new WeightedAvgAccumulator();
        }

        @Override
        public Long getValue(WeightedAvgAccumulator acc) {
            if (acc.count == 0) {
                return null;
            } else {
                return acc.sum / acc.count;
            }
        }

        public void accumulate(WeightedAvgAccumulator acc, Integer iWeight) {
            acc.sum += iWeight;
            acc.count += 1;
        }

        public void retract(WeightedAvgAccumulator acc,Integer iWeight) {
            acc.sum -= iWeight;
            acc.count -= 1;
        }

        public void merge(WeightedAvgAccumulator acc, Iterable<WeightedAvgAccumulator> it) {
            for (WeightedAvgAccumulator a : it) {
                acc.count += a.count;
                acc.sum += a.sum;
            }
        }

        public void resetAccumulator(WeightedAvgAccumulator acc) {
            acc.count = 0;
            acc.sum = 0L;
        }
    }

    public static class WeightedAvgAccumulator {
        public long sum = 0;
        public int count = 0;
    }
}
