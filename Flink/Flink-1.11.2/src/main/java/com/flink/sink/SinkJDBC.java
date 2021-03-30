package com.flink.sink;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.flink.bean.SensorReading;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;

/**
 * sink_JDBC
 */
public class SinkJDBC {
    public static void main(String[] args) {
        // 从kafka中读取topic数据
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // kafka 配置项
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "dxbigdata103:9092");
        DataStreamSource<String> streamSource = env.addSource(new FlinkKafkaConsumer011<String>("GMALL_STARTUP", new SimpleStringSchema(), properties));
        // 进行相关ETL
        // 将kafka数据转换成SensorReading类型
        // 先过滤到不符规范的json数据
        SingleOutputStreamOperator<String> filter = streamSource.filter(new FilterFunction<String>() {
            public boolean filter(String s) throws Exception {
                try {
                    JSONObject jsonObject = JSON.parseObject(s);
                    return  true;
                }catch (Exception e){
                    System.out.println("此次json不符合规范");
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

        DataStreamSink<SensorReading> jdbcSink = mapOperator.addSink(new MyJdbcSink());
    }
    public static class MyJdbcSink extends RichSinkFunction<SensorReading> {
        Connection conn = null;
        PreparedStatement insertStmt = null;
        PreparedStatement updateStmt = null;
        // open 主要是创建连接
        @Override
        public void open(Configuration parameters) throws Exception {
            conn = DriverManager.getConnection("jdbc:mysql://dxbigdata103:3306/test","root", "000000");
        // 创建预编译器，有占位符，可传入参数
            insertStmt = conn.prepareStatement("INSERT INTO sensor_temp (id, temp) VALUES(?, ?)");
            updateStmt = conn.prepareStatement("UPDATE sensor_temp SET temp = ? WHERE id = ?");
        }
        // 调用连接，执行 sql
        @Override
        public void invoke(SensorReading value, Context context) throws Exception {
        // 执行更新语句，注意不要留 super
            updateStmt.setString(1, value.getUid());
            updateStmt.setString(2, value.getMid());
            updateStmt.execute();
            // 如果刚才 update 语句没有更新，那么插入
            if (updateStmt.getUpdateCount() == 0) {
                insertStmt.setString(1, value.getMid());
                insertStmt.setString(2,value.getUid());
                insertStmt.execute();
            }
        }
        @Override
        public void close() throws Exception {
            insertStmt.close();
            updateStmt.close();
            conn.close();
        }
    }
}
