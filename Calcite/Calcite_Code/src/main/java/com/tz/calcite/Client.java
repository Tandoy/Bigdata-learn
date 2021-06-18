package com.tz.calcite;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.net.URL;
import java.net.URLDecoder;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Client {
    /**
     * 测试的时候用字符串 defaultSchema 默认数据库 name 数据库名称 type custom factory
     * 请求接收类，该类会实例化Schema也就是数据库类，Schema会实例化Table实现类，Table会实例化数据类。
     * operand 动态参数，ScheamFactory的create方法会接收到这里的数据
     */
    public static void main(String[] args) {
        try {
            // 用文件的方式
            URL url = Client.class.getResource("/model.json");
            String str = URLDecoder.decode(url.toString(), "UTF-8");
            Properties info = new Properties();
            info.put("model", str.replace("file:", ""));
            Connection connection = DriverManager.getConnection("jdbc:calcite:", info);
            Statement statement = connection.createStatement();
            test1(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * CSV文件读取
     *
     * @param statement
     * @throws Exception
     */
    public static void test1(Statement statement) throws Exception {
        ResultSet resultSet = statement.executeQuery("select * from test_csv.TEST01");
        System.out.println(JSON.toJSONString(getData(resultSet)));
    }


    public static List<Map<String, Object>> getData(ResultSet resultSet) throws Exception {
        List<Map<String, Object>> list = Lists.newArrayList();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnSize = metaData.getColumnCount();

        while (resultSet.next()) {
            Map<String, Object> map = Maps.newLinkedHashMap();
            for (int i = 1; i < columnSize + 1; i++) {
                map.put(metaData.getColumnLabel(i), resultSet.getObject(i));
            }
            list.add(map);
        }
        return list;
    }
}
