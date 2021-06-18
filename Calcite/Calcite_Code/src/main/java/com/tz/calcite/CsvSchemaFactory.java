package com.tz.calcite;

import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaFactory;
import org.apache.calcite.schema.SchemaPlus;

import java.util.Map;

/**
 * calcite中，引入一个数据库通常是通过注册一个SchemaFactory接口实现类来实现。
 * SchemaFactory中只有一个方法，就是生成Schema。Schema最重要的功能是获取所有Table。
 * Table有两个功能，一个是获取所有字段的类型，另一个是得到Enumerable迭代器用来读取数据。
 */
public class CsvSchemaFactory implements SchemaFactory {

    /**
     * @param schemaPlus 他的父节点，一般为root
     * @param s          数据库的名字，它在model中定义的
     * @param map        也是在mode中定义的，是Map类型，用于传入自定义参数。
     * @return
     */
    public Schema create(SchemaPlus schemaPlus, String s, Map<String, Object> map) {
        return new CsvSchema(String.valueOf(map.get("dataFile")));
    }
}
