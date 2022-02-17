package com.tz.calcite;

import com.google.gson.Gson;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.dialect.MysqlSqlDialect;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;


import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class MultiSource {
    public static void main(String[] args)  throws Exception{
        String filepath = "D:\\Downloads\\github\\Bigdata-learn\\Calcite\\Calcite_Code\\src\\main\\resources\\multiSource.json";
        Properties config = new Properties();
        config.put("model",filepath);
        config.put("lex", "MYSQL");
        String sql =
                "SELECT o.oid,o.iid,o.icount,i.catalog,i.pname,i.price FROM gbasedbt.order_table AS o join mysql.item AS i on o.iid = i.i_id";

        try (Connection con = DriverManager.getConnection("jdbc:calcite:", config)) {
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    //打印逻辑计划
                    printLogicPlan(filepath,sql);
                    //打印查询结果
                    printRs(rs);
                }
            }
        }

    }

    public static void printRs(ResultSet rs) throws Exception {
        ResultSetMetaData rsmd = rs.getMetaData();
        int count = rsmd.getColumnCount();

        for(int i = 1; i <= count; i++){
            System.out.print(rsmd.getColumnName(i)+"\t");
        }
        System.out.println();

        while(rs.next()){
            for(int i = 1; i <= count; i++){
                System.out.print(rs.getString(i)+"\t");
            }
            System.out.println();
        }
    }

    public static void printLogicPlan(String modelPath , String sql) throws Exception{

        String modelJsonStr = Files.readAllLines(Paths.get(modelPath)).stream().collect(Collectors.joining("\n"));
        HashMap map = new Gson().fromJson(modelJsonStr, HashMap.class);
        List<Map> schemas = (List<Map>) map.get("schemas");

        SchemaPlus rootSchema = Frameworks.createRootSchema(true);
        Schema gbasedbt = JdbcSchema.create(rootSchema, "gbasedbt" , (Map<String,Object>)schemas.get(1).get("operand"));
        Schema mysql = JdbcSchema.create(rootSchema, "mysql" , (Map<String,Object>)schemas.get(0).get("operand"));
        rootSchema.add("gbasedbt",gbasedbt);
        rootSchema.add("mysql",mysql);

        SqlParser.Config insensitiveParser = SqlParser.configBuilder()
                .setCaseSensitive(false)
                .build();

        FrameworkConfig config = Frameworks.newConfigBuilder()
                .parserConfig(insensitiveParser)
                .defaultSchema(rootSchema)
                .build();

        Planner planner = Frameworks.getPlanner(config);
        SqlNode sqlNode = planner.parse(sql);
        SqlNode sqlNodeValidated = planner.validate(sqlNode);
        RelRoot relRoot = planner.rel(sqlNodeValidated);
        RelNode relNode = relRoot.project();

        System.out.println(sqlNode.toSqlString(MysqlSqlDialect.DEFAULT));
        System.out.println();
//        System.out.println(relNode.explain(););
    }
}
