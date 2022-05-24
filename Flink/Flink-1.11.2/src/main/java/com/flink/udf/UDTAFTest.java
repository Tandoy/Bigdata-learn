package com.flink.udf;

import com.flink.bean.Event;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.functions.TableAggregateFunction;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;

import static org.apache.flink.table.api.Expressions.$;
import static org.apache.flink.table.api.Expressions.call;


public class UDTAFTest {
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
                    sourceContext.collect(new Event("panda", 200));
                    sourceContext.collect(new Event("panda", 500));
                    sourceContext.collect(new Event("dog", 120));
                    sourceContext.collect(new Event("dog", 250));
                    sourceContext.collect(new Event("dog", 200));
                    Thread.sleep(5000);
                }
            }

            @Override
            public void cancel() {

            }
        });

        tableEnv.registerDataStream("mytable",streamSource);

        // register function
        tableEnv.createTemporarySystemFunction("Top2", Top2.class);

        // call registered function in Table API
        Table table = tableEnv.from("mytable")
                .groupBy("name")
                .flatAggregate(call(Top2.class, $("type")))
                .select($("name"), $("f0"), $("f1"));

//        Table table = tableEnv.sqlQuery("SELECT name,Top2(type) FROM mytable group by name");

        // sink
        tableEnv.toRetractStream(table, Row.class).print();

        // execute
        env.execute("UDTAFTest");
    }

    public static class Top2 extends TableAggregateFunction<Tuple2<Integer, Integer>, Top2Accumulator> {
        @Override
        public Top2Accumulator createAccumulator() {
            Top2Accumulator acc = new Top2Accumulator();
            acc.first = Integer.MIN_VALUE;
            acc.second = Integer.MIN_VALUE;
            return acc;
        }

        public void accumulate(Top2Accumulator acc, Integer value) {
            if (value > acc.first) {
                acc.second = acc.first;
                acc.first = value;
            } else if (value > acc.second) {
                acc.second = value;
            }
        }

        public void merge(Top2Accumulator acc, Iterable<Top2Accumulator> it) {
            for (Top2Accumulator otherAcc : it) {
                accumulate(acc, otherAcc.first);
                accumulate(acc, otherAcc.second);
            }
        }

        public void emitValue(Top2Accumulator acc, Collector<Tuple2<Integer, Integer>> out) {
            // emit the value and rank
            if (acc.first != Integer.MIN_VALUE) {
                out.collect(Tuple2.of(acc.first, 1));
            }
            if (acc.second != Integer.MIN_VALUE) {
                out.collect(Tuple2.of(acc.second, 2));
            }
        }

    }

    public static class Top2Accumulator {
        public Integer first;
        public Integer second;
    }
}
