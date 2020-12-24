# -*- coding: utf-8 -*-
# @time :2019/11/5 16:13
# @File :sqoop_export.py
import os
from airflow import configuration
from airflow_cfg import GET_CONN


def get_conn(server_db, i):
    conn_i = configuration.get(server_db, i)
    return conn_i


def sqoop_export(**kwargs):
    # airflow脚本传mysql_db参数，通过get_conn方法获得mysql数据库链接信
    mysql_db = kwargs['mysql_db']
    Mhost = get_conn(mysql_db, 'host')
    Musername = get_conn(mysql_db, 'username')
    Mpassword = get_conn(mysql_db, 'password')
    Mdb = get_conn(mysql_db, 'database')
    Htable = kwargs['Htable']
    Mtable = Htable
    Hdb = kwargs['Hdb']
    Mtablekey = kwargs['Mtablekey']
    sqoop = "sqoop export --connect jdbc:mysql://" \
            + Mhost + ":3306/" \
            + Mdb + " --username " \
            + Musername + " --password " \
            + Mpassword + " --table " + Mtable + " --fields-terminated-by ',' --export-dir /user/hive/warehouse/" \
            + Hdb + ".db/" + Htable + " --update-key " + Mtablekey + " --update-mode allowinsert"
    n1 = os.system(sqoop)
    print(sqoop)

#MySQL临时层导数脚本
def sqoop_export_tmp_umkt(**kwargs):
    # airflow脚本传mysql_db参数，通过get_conn方法获得mysql数据库链接信
    mysql_db = 'creditlife_umkt'
    Mhost = get_conn(mysql_db, 'host')
    Musername = get_conn(mysql_db, 'username')
    Mpassword = get_conn(mysql_db, 'password')
    Mdb = get_conn(mysql_db, 'database')
    Hdb = kwargs['Hdb']
    Htable = kwargs['Htable']
    Mtablekey = kwargs['Mtablekey']
    Mtable = Htable
    sqoop = "sqoop export --connect 'jdbc:mysql://" \
            + Mhost + ":3306/" \
            + Mdb + "?useUnicode=true&characterEncoding=utf-8' --username " \
            + Musername + " --password " \
            + Mpassword + " --table " + Mtable + "_tmp" + " --fields-terminated-by '\\001' --export-dir /user/hive/warehouse/" \
            + Hdb + ".db/" + Htable + " --input-null-string '\\\\N' --input-null-non-string '\\\\N'" \
            + " --update-key " + Mtablekey + " --update-mode allowinsert"
    n1 = os.system(sqoop)
    print(sqoop)

#经检查后当日跑批数据无误，批量导数至bank_db
def sqoop_export_real_umkt(**kwargs):
   # airflow脚本传mysql_db参数，通过get_conn方法获得mysql数据库链接信
    mysql_db = 'creditlife_umkt'
    Mhost = get_conn(mysql_db, 'host')
    Musername = get_conn(mysql_db, 'username')
    Mpassword = get_conn(mysql_db, 'password')
    Mdb = get_conn(mysql_db, 'database')
    Hdb = kwargs['Hdb']
    Htable = kwargs['Htable']
    Mtablekey = kwargs['Mtablekey']
    Mtable = Htable
    sqoop = "sqoop export --connect 'jdbc:mysql://" \
            + Mhost + ":3306/" \
            + Mdb + "?useUnicode=true&characterEncoding=utf-8' --username " \
            + Musername + " --password " \
            + Mpassword + " --table " + Mtable + " --fields-terminated-by '\\001' --export-dir /user/hive/warehouse/" \
            + Hdb + ".db/" + Htable + " --input-null-string '\\\\N' --input-null-non-string '\\\\N'" \
            + " --update-key " + Mtablekey + " --update-mode allowinsert"
    n1 = os.system(sqoop)
    print(sqoop)


# 经检查后当日跑批数据无误，批量导数至wcl_dwh
def sqoop_export_real_bi(**kwargs):
    # airflow脚本传mysql_db参数，通过get_conn方法获得mysql数据库链接信
    mysql_db = 'creditlife_bi'
    Mhost = get_conn(mysql_db, 'host')
    Musername = get_conn(mysql_db, 'username')
    Mpassword = get_conn(mysql_db, 'password')
    Mdb = get_conn(mysql_db, 'database')
    Hdb = kwargs['Hdb']
    Htable = kwargs['Htable']
    Mtablekey = kwargs['Mtablekey']
    Mtable = Htable
    sqoop = "sqoop export --connect 'jdbc:mysql://" \
            + Mhost + ":3306/" \
            + Mdb + "?useUnicode=true&characterEncoding=utf-8' --username " \
            + Musername + " --password " \
            + Mpassword + " --table " + Mtable + " --fields-terminated-by '\\001' --export-dir /user/hive/warehouse/" \
            + Hdb + ".db/" + Htable + " --input-null-string '\\\\N' --input-null-non-string '\\\\N'" \
            + " --update-key " + Mtablekey + " --update-mode allowinsert"
    n1 = os.system(sqoop)
    print(sqoop)
