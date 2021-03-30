package com.flink.tableApi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.flink.bean.SensorReading;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.table.api.Over;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.Tumble;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

import java.util.Properties;

/**
 * table api 事件事件、处理时间
 */
public class TimeAndWindow {
    public static void main(String[] args) throws Exception {
        LocalStreamEnvironment streamEnvironment = StreamExecutionEnvironment.createLocalEnvironment();
        StreamTableEnvironment tableEnvironment = StreamTableEnvironment.create(streamEnvironment);
        // kafka 配置项
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "dxbigdata103:9092");
        DataStreamSource<String> streamSource = streamEnvironment.addSource(new FlinkKafkaConsumer011<String>("GMALL_STARTUP", new SimpleStringSchema(), properties));

        // 将kafka数据转换成SensorReading类型
        // 先过滤到不符规范的json数据
        SingleOutputStreamOperator<String> filter = streamSource.filter(new FilterFunction<String>() {
            public boolean filter(String s) throws Exception {
                try {
                    JSONObject jsonObject = JSON.parseObject(s);
                    return  true;
                }catch (Exception e){
                    System.out.println("此json不符合规范");
                }
                return  false;
            }
        });
        // 这里使用fastjson解析flink流式处理拿到的json数据
        DataStream<SensorReading> mapOperator = filter.map(new MapFunction<String, SensorReading>() {
            public SensorReading map(String s) throws Exception {
                JSONObject parseObject = JSON.parseObject(s);
                return  new SensorReading(parseObject.getString("area"),parseObject.getString("uid"),
                        parseObject.getString("os"),parseObject.getString("ch"),parseObject.getString("appid")
                        ,parseObject.getString("mid"),parseObject.getString("type"),parseObject.getString("vs")
                        ,parseObject.getString("ts"));
            }
        });

        // 处理时间
        Table dataTable = tableEnvironment.fromDataStream(mapOperator, "id, timestamp as ts, temperature as temp, pt.proctime");
        dataTable.printSchema();
        tableEnvironment.toAppendStream(dataTable, Row.class).print();

        // 聚合函数

        tableEnvironment.createTemporaryView("sensor", dataTable);

        // 5. 窗口操作
        // 5.1 Group Window
        // table API
        Table resultTable = dataTable.window(Tumble.over("10.seconds").on("rt").as("tw"))
                .groupBy("id, tw")
                .select("id, id.count, temp.avg, tw.end");

        // SQL
        Table resultSqlTable = tableEnvironment.sqlQuery("select id, count(id) as cnt, avg(temp) as avgTemp, tumble_end(rt, interval '10' second) " +
                "from sensor group by id, tumble(rt, interval '10' second)");

        // 5.2 Over Window
        // table API
        Table overResult = dataTable.window(Over.partitionBy("id").orderBy("rt").preceding("2.rows").as("ow"))
                .select("id, rt, id.count over ow, temp.avg over ow");

        // SQL
        Table overSqlResult = tableEnvironment.sqlQuery("select id, rt, count(id) over ow, avg(temp) over ow " +
                " from sensor " +
                " window ow as (partition by id order by rt rows between 2 preceding and current row)");

//        dataTable.printSchema();
//        tableEnv.toAppendStream(resultTable, Row.class).print("result");
//        tableEnv.toRetractStream(resultSqlTable, Row.class).print("sql");
        tableEnvironment.toAppendStream(overResult, Row.class).print("result");
        tableEnvironment.toRetractStream(overSqlResult, Row.class).print("sql");
        streamEnvironment.execute();

    }
}
