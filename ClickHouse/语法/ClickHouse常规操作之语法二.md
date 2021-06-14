## ClickHouse之语法

### 6.DDL

    6.1.数据表DDL
    
    alter table tb_par add column score UInt8;
    alter table tb_par drop column score;
    rename table tb_par to tb_par_par; --同数据库表重命名
    rename table tb_par to defalut.tb_par_par; --移动表至其它数据库
    
    1.只支持MergeTree、Merge、Distributed三种表引擎
    2.不能修改排序字段以及主键
    
    6.2.分区表
    
    alter table tb_par drop partition 20201231; --删除指定分区数据
    alter table tb_par1 replace partition 20201231 from tb_par --替换分区数据
    alter table tb_par clear column name in partition 20201231 --清除指定表指定分区指定列数据
    alter table tb_par detach partition 20201231 --卸载分区数据
    alter table tb_par attach partition 20201231 --装载分区数据
    
### 7.DML

    7.1 导入数据
    
    insert into tb_a values(1,'tangzhi',23,'M');
    insert into tb_a (c1,c2,c3,c4) values(1,'tangzhi',23,'M');
    insert into tb_a select * from tb_b;
    create table tb_name engine=Log as select * from xxx;
    clickhouse client -n -q 'insert into tb_name FORMAT CSV' < ./a.txt;
    clickhouse client --format_csv_delimiter=',' -q 'insert into tb_name FORMAT CSV' < ./a.txt;
    
    7.2 删除数据(仅支持MergeTree引擎)
    
    alter table tb_b delete where data_date = 20201231;
    
    7.3 更新数据(不能更改排序、主键以及分区字段)
    
    alter table tb_b update name = 'zss' where data_date = 20201231;
    
    