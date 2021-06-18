package com.tz.calcite.mem;

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
            URL url = Client.class.getResource("/model_mem.json");
            String str = URLDecoder.decode(url.toString(), "UTF-8");
            Properties info = new Properties();
            info.put("model", str.replace("file:", ""));
            Connection connection = DriverManager.getConnection("jdbc:calcite:", info);
            Statement statement = connection.createStatement();

            test2(statement);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * CSV文件读取
     * @param statement
     * @throws Exception
     */
    public static void test1(Statement statement) throws Exception {
        ResultSet resultSet = statement.executeQuery("select * from test_csv.TEST01");
        System.out.println(JSON.toJSONString(getData(resultSet)));
    }

    /**
     * CSV文件与内存文件关联读取
     * @param statement
     * @throws Exception
     */
    public static void test2(Statement statement) throws Exception {
        ResultSet resultSet1 = statement.executeQuery("select csv1.id as cid,csv1.name1 as cname ,mem1.id as mid,mem1.mem_str as mstr from test_csv.TEST01 as csv1 left join test_mem.mem_table_1 as mem1 on csv1.id = mem1.id");
        System.out.println(JSON.toJSONString(getData(resultSet1)));
    }

    public static List<Map<String,Object>> getData(ResultSet resultSet)throws Exception{
        List<Map<String,Object>> list = Lists.newArrayList();
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
