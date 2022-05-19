package com.flink.cep;

import com.flink.bean.Event;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.functions.PatternProcessFunction;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 测试 Flink CEP
 * 现在我们定义规则，对于某个用户，若满足下面其中一个条件，就会被标记为潜在客户，可能会作为后续商品的推广对象。
 *      1. 先点击浏览商品，然后将商品加入收藏。
 *      2. 1分钟内点击浏览了商品3次。
 */
public class DetectPotentialCustomerByCEP {
    public static void main(String[] args) throws Exception {
        // 1.create
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 2.env相关设置
        env.setParallelism(1);
//        env.enableCheckpointing(5000);
//        env.disableOperatorChaining();
//        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
//        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
//        env.setStateBackend(new FsStateBackend("file:///opt/flink-1.13.5/checkpoint"));

        // 3.source
        SingleOutputStreamOperator<String> dataStreamSource = env.addSource(new SourceFunction<String>() {
            @Override
            public void run(SourceContext<String> sourceContext) throws Exception {
                while (true) {
                    sourceContext.collect("jack,0");
                    sourceContext.collect("jack,1");
                    sourceContext.collect("andy,0");
                    sourceContext.collect("andy,0");
                    sourceContext.collect("andy,0");
                    Thread.sleep(500);
                }
            }

            @Override
            public void cancel() {

            }
        }).name("source");

        // 4.transformation
        KeyedStream<Event, String> cepInput = dataStreamSource.filter(new FilterFunction<String>() {
            @Override
            public boolean filter(String s) throws Exception {
                return !s.isEmpty();
            }
        }).map(new MapFunction<String, Event>() {
            @Override
            public Event map(String record) throws Exception {
                // 输入的string，逗号分隔，第一个字段为用户名，第二个字段为事件类型
                // Jack,0
                // Jack,1
                String[] split = record.split(",");
                if (split.length != 2) {
                    return null;
                }
                Event event = new Event();
                event.setName(split[0]);
                event.setType(Integer.parseInt(split[1]));
                return event;
            }
        }).keyBy(record -> record.getName());
        /**
         * next() --> Strict Contiguity：要求一个event之后必须紧跟下一个符合条件的event，中间不允许有其他事件。
         * followedBy() --> Relaxed Contiguity：和上一种不同的是，该模式允许中间有其他无关的event，会对他们进行忽略。
         * followedByAny() --> Non-Deterministic Relaxed Contiguity：非确定性宽松连续性，可以对已经匹配的事件就行忽略，对接下来的事件继续匹配。
         */
        // 5.patternA : 先点击浏览商品，然后将商品加入收藏
        Pattern<Event, ?> patternA = Pattern.<Event>begin("firstly")
                .where(new SimpleCondition<Event>() {
                    @Override
                    public boolean filter(Event event) throws Exception {
                        // 先浏览商品
                        return event.getType() == 0;
                    }
                })
                .followedBy("and")
                .where(new SimpleCondition<Event>() {
                    @Override
                    public boolean filter(Event event) throws Exception {
                        // 然后将商品加入收藏
                        return event.getType() == 1;
                    }
                });
        // 6.patternB : 1分钟内点击浏览了商品3次
        Pattern<Event, ?> patternB = Pattern.<Event>begin("start").where(new SimpleCondition<Event>() {
            @Override
            public boolean filter(Event event) throws Exception {
                // 浏览商品
                return event.getType() == 0;
            }
        }).timesOrMore(3)
                .within(Time.minutes(1));

        // 7.CEP用pattern将输入的时间事件流转化为复杂事件流
        // Flink在1.12版本之后，PatternStream默认使用Event Time。如果业务使用的事Processing Time，必须要明确配置！否者没有数据流向下游
        PatternStream<Event> patternStreamA = CEP.pattern(cepInput, patternA).inProcessingTime();
        PatternStream<Event> patternStreamB = CEP.pattern(cepInput, patternB).inProcessingTime();

        DataStream<String> streamA = processPatternStream(patternStreamA, "收藏商品");
        DataStream<String> streamB = processPatternStream(patternStreamB, "连续浏览商品");

        // 8.union
        DataStream<String> unionStream = streamA.union(streamB);

        // 9.sink
        unionStream.print().setParallelism(1);

        // 10.execute
        env.execute("Flink Streaming Java API CEP");
    }

    private static DataStream<String> processPatternStream(PatternStream<Event> patternStream, String tag) {
        return patternStream.process(new PatternProcessFunction<Event, String>() {
            @Override
            public void processMatch(Map<String, List<Event>> map, Context context, Collector<String> collector) throws Exception {
                String name = null;
                Iterator<Map.Entry<String, List<Event>>> iterator = map.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, List<Event>> event = iterator.next();
                    name = event.getValue().get(0).getName();
                }
                collector.collect(name + " 成为潜在客户 ," + tag);
            }
        });
    }

}

