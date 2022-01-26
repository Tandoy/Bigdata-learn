package com.flink.functions;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

public class SimpleSourceFunction implements SourceFunction<Tuple3<String, Integer, Long>> {
    @Override
    public void run(SourceFunction.SourceContext<Tuple3<String, Integer, Long>> ctx) throws Exception {
        int index = 1;
        while (true) {
            ctx.collect(new Tuple3<>("key", ++index, System.currentTimeMillis()));
//            ctx.collect(new Tuple3<>("key2", index, System.currentTimeMillis()));
//            ctx.collect(new Tuple3<>("key3", index, System.currentTimeMillis()));
            Thread.sleep(500);
        }
    }

    @Override
    public void cancel() {

    }
}
