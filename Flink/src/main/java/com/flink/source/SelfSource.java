package com.flink.source;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import com.flink.bean.SensorReading;

import java.util.HashMap;
import java.util.Random;

public class SelfSource {
    public static void main(String[] args) {
        // 自定义source
        StreamExecutionEnvironment streamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        streamExecutionEnvironment.addSource(new MySource());
    }
    public static class MySource implements SourceFunction<SensorReading> {
        // 定义数据源是否正常运行的标志
        private Boolean run_flag = true;
        public void run(SourceContext<SensorReading> sourceContext) throws Exception {
            Random random = new Random();
            HashMap<String, Double> sensorTempMap = new HashMap<String, Double>();
            for( int i = 0; i < 10; i++ ){
                sensorTempMap.put("sensor_" + (i + 1), 60 + random.nextGaussian() * 20);
            }
            while (run_flag) {
                for( String sensorId: sensorTempMap.keySet() ){
                    Double newTemp = sensorTempMap.get(sensorId) + random.nextGaussian();
                    sensorTempMap.put(sensorId, newTemp);
                    sourceContext.collect( new SensorReading(sensorId, "",newTemp.toString(),"","","","","",""));
                }
                Thread.sleep(1000L);
            }
        }

        public void cancel() {
            run_flag = false;
        }
    }
}

