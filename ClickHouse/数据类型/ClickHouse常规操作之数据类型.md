## ClickHouse之数据类型

### 1.建库建表（建表必须指定引擎类型）

    create database if not exists tangzhi;
    use tangzhi;
    create table tb_a(
    id Int32,
    name String,
    age UInt8,
    gendrer String
    )engine=Memory();
    
### 2.插入数据

    insert into tb_a values(1,'tangzhi',23,'M');--字符串必须使用单引号
    
### 3.数据类型

    1.[数组]
        ck是强数据类型 array()/[];
        create table test_array(
        id Int32,
        name Array(String)
        )engine=Log;
        
        insert into test_array values(1,array('ww','ww'));
        insert into test_array values(2,array('www','www'));
        select *,name[2] from test_array;--数据下标从1-based
        select arrayMap(e -> concat(e,'abc'),name),name from test_array; --支持高阶表达式
        
    2.[枚举]
        create table test_enum(
        id Int32,
        name Enum('RED'=1,'GREEN'=2,'BLUE'=3)
        )engine=Memory();
        
        insert into test_enum values(1,'RED');
        insert into test_enum values(3,'PINK'); --没有声明的无法插入数据
        insert into test_enum values(4,1),(5,2),(6,3); --这样查询效率更高
        
    3.[元组]
        一种特殊的数据类型  集合
        create table test_tuple(
        id Int32,
        name Tuple(String,String,UInt8)
        )engine=Memory();
        
        select tuple(1,2,'tangzhi');
        select tuple(1,2,'tangzhi') as x,toTypeName(x);
        
        insert into test_tuple values(1,('coder','M',1));
        select id,name.1 from test_tuple;
        
    4.[Nested]
        嵌套数据类型
        create table test_nested(
        id Int32,
        name Nested(
          uid Int32
          ,hname String
        )
        )engine=Memory();
        
        insert into test_nested values(1,[1,2],['eat','drink']);
        select id,name.uid,name.hname,name.hname[1] from test_nested;
        
    5.[Domain]
        可对特殊实体类进行校验
        create table test_domain(
        id Int32,
        ip IPv4
        )engine=Memory();
        
        insert into test_domain values(1,'192.16.1.1')
        
               