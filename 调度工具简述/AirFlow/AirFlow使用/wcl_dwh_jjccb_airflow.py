# -*- coding: utf-8 -*-

import airflow
from airflow import DAG
from airflow.operators.bash_operator import BashOperator
from airflow.operators.python_operator import PythonOperator
from airflow.operators.hive_operator import HiveOperator
from datetime import datetime,timedelta
import sys
reload(sys)
sys.setdefaultencoding("utf-8")

from stg_card_acct_cu_sql_class import SQL_stg_card_acct_cu
from stg_card_card_cu_sql_class import SQL_stg_card_card_cu
from stg_card_ccdbiz_cu_sql_class import SQL_stg_card_ccdbiz_cu
from stg_card_mpur_cu_sql_class import SQL_stg_card_mpur_cu
from dwd_card_acct_sql_class import SQL_dwd_card_acct
from dwd_card_acctbal_hs_sql_class import SQL_dwd_card_acctbal_hs
from dwd_card_apma_cu_sql_class import SQL_dwd_card_apma_cu
from dwd_card_card_sql_class import SQL_dwd_card_card
from dwd_card_cardoper_hs_sql_class import SQL_dwd_card_cardoper_hs
from dwd_card_ccdbiz_sql_class import SQL_dwd_card_ccdbiz
from dwd_card_ccdcust_sql_class import SQL_dwd_card_ccdcust
from dwd_card_other_sql_class import SQL_dwd_card_other
from dws_card_income_sql_class import SQL_dws_card_income
from dws_card_mpur_a_d_sql_class import SQL_dws_card_mpur
from dws_card_txn_sql_class import SQL_dws_card_txn
from ads_bi_risk_sql_class import SQL_ads_bi_risk
from ads_bi_umkt_sql_class import SQL_ads_bi_umkt
from ads_umkt_sql_class import SQL_ads_umkt
from apt_card_sql_class import SQL_apt_card

default_args = {
    'owner': 'tzhi',   # 拥有者名称
    'depends_on_past': True, # 是否依赖上一个自己的执行状态
    'start_date': airflow.utils.dates.days_ago(2), # 第一次开始执行的时间，为格林威治时间，为了方便测试，一般设置为当前时间减去执行周期 T-1/T-2
    'email': ['tangzhi8023@gmail.com'], # 接收通知的email列表
    'email_on_failure': True, # 是否在任务执行失败时接收邮件
      'email_on_retry': True, # 是否在任务重试时接收邮件
    'retries': 3, # 失败重试次数
    'retry_delay': timedelta(minutes=5), # 失败重试间隔
    # 'queue': 'bash_queue',
    # 'pool': 'backfill',
    # 'priority_weight': 10,
    # 'end_date': datetime(2016, 1, 1),
    # 'wait_for_downstream': False,
    # 'dag': dag,
    # 'adhoc':False,
    # 'sla': timedelta(hours=2),
    # 'execution_timeout': timedelta(seconds=300),
    # 'on_failure_callback': some_function,
    # 'on_success_callback': some_other_function,
    # 'on_retry_callback': another_function,
    # 'trigger_rule': u'all_success'
}

#-------------------------------------------------------------------------------
# dag

dag = DAG(
    'wcl_dwh', # dag_id
    default_args=default_args, # 指定默认参数
    description='jjcbk_yy',
    schedule_interval=timedelta(days=1)) # 执行周期，依次是分，时，天，月，年，此处表示每个整点执行;此处表示每天执行一次
#apt
sqlapt = SQL_apt_card()
apt_card_acct_cu = HiveOperator(
      task_id = "apt_card_acct_cu",
      hql = sqlapt.sqlapt_card_acct_cu,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

apt_card_apma_cu = HiveOperator(
      task_id = "apt_card_apma_cu",
      hql = sqlapt.sqlapt_card_apma_cu,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

apt_card_appr_cu = HiveOperator(
      task_id = "apt_card_appr_cu",
      hql = sqlapt.sqlapt_card_appr_cu,
      hive_cli_conn_id = 'hive',
      dag = dag
	)


apt_card_card_cu = HiveOperator(
      task_id = "apt_card_card_cu",
      hql = sqlapt.sqlapt_card_card_cu,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

apt_card_ccdcust_cu = HiveOperator(
      task_id = "apt_card_ccdcust_cu",
      hql = sqlapt.sqlapt_card_ccdcust_cu,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

apt_card_chgs_cu = HiveOperator(
      task_id = "apt_card_chgs_cu",
      hql = sqlapt.sqlapt_card_chgs_cu,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

apt_card_cnta_cu = HiveOperator(
      task_id = "apt_card_cnta_cu",
      hql = sqlapt.sqlapt_card_cnta_cu,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

apt_card_commtxn_cu = HiveOperator(
      task_id = "apt_card_commtxn_cu",
      hql = sqlapt.sqlapt_card_commtxn_cu,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

apt_card_jorj_cu = HiveOperator(
      task_id = "apt_card_jorj_cu",
      hql = sqlapt.sqlapt_card_jorj_cu,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

apt_card_mpur_cu = HiveOperator(
      task_id = "apt_card_mpur_cu",
      hql = sqlapt.sqlapt_card_mpur_cu,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

apt_card_stmt_cu = HiveOperator(
      task_id = "apt_card_stmt_cu",
      hql = sqlapt.sqlapt_card_stmt_cu,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

apt_card_txn_cu = HiveOperator(
      task_id = "apt_card_txn_cu",
      hql = sqlapt.sqlapt_card_txn_cu,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

apt_card_cardoper_cu = HiveOperator(
      task_id = "apt_card_cardoper_cu",
      hql = sqlapt.sqlapt_card_cardoper_cu,
      hive_cli_conn_id = 'hive',
      dag = dag
      )

apt_pboc_queryinfo = HiveOperator(
      task_id = "apt_pboc_queryinfo",
      hql = sqlapt.sqlapt_pboc_queryinfo,
      hive_cli_conn_id = 'hive',
      dag = dag
      )

apt_pboc_idvcrrprtnum = HiveOperator(
      task_id = "apt_pboc_idvcrrprtnum",
      hql = sqlapt.sqlapt_pboc_idvcrrprtnum,
      hive_cli_conn_id = 'hive',
      dag = dag
      )

# stg账户表
sqlacct = SQL_stg_card_acct_cu()
stg_card_acct_cu = HiveOperator(
      task_id = "stg_card_acct_cu",
      hql = sqlacct.sqlstg_acct,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

# stg卡表
sqlcard = SQL_stg_card_card_cu()
stg_card_card_cu = HiveOperator(
      task_id = "stg_card_card_cu",
      hql = sqlcard.sqlstg_card,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

# stg业务条线表
sqlccdbiz = SQL_stg_card_ccdbiz_cu()
stg_card_ccdbiz_cu = HiveOperator(
      task_id = "stg_card_ccdbiz_cu",
      hql = sqlccdbiz.sqlstg_ccdbiz,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

# stg分期表
sqlmpur = SQL_stg_card_mpur_cu()
stg_card_mpur_cu = HiveOperator(
      task_id = "stg_card_mpur_cu",
      hql = sqlmpur.sqlstg_mpur,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

# dwd账户全量快照表
sqldwdacct = SQL_dwd_card_acct()
dwd_card_acct_cu = HiveOperator(
      task_id = "dwd_card_acct_cu",
      hql = sqldwdacct.sqldwd_acct_cu,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

# dwd账户账单快照表
dwd_card_acct_cyc = HiveOperator(
      task_id = "dwd_card_acct_cyc",
      hql = sqldwdacct.sqldwd_acct_cyc,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

# dwd账户自然月快照表
dwd_card_acct_mth = HiveOperator(
      task_id = "dwd_card_acct_mth",
      hql = sqldwdacct.sqldwd_acct_mth,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

# dwd账户余额历史表
sqldwdacctbal_hs = SQL_dwd_card_acctbal_hs()
dwd_card_acctbal_hs = HiveOperator(
      task_id = "dwd_card_acctbal_hs",
      hql = sqldwdacctbal_hs.sqldwd_acctbal_hs,
      hive_cli_conn_id = 'hive',
      dag = dag
      )


# dwd申请表
sqldwdapma = SQL_dwd_card_apma_cu()
dwd_card_apma_cu = HiveOperator(
      task_id = "dwd_card_apma_cu",
      hql = sqldwdapma.sqldwd_apma,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

# dwd卡全量快照表
sqldwdcard_card = SQL_dwd_card_card()
dwd_card_card_cu = HiveOperator(
      task_id = "dwd_card_card_cu",
      hql = sqldwdcard_card.sqldwd_card_card_cu,
      hive_cli_conn_id = 'hive',
      dag = dag
	)


# dwd卡自然月快照表
dwd_card_card_mth = HiveOperator(
      task_id = "dwd_card_card_mth",
      hql = sqldwdcard_card.sqldwd_card_card_mth,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

# dwd卡维护历史比表
dwd_card_cardoper_hs = SQL_dwd_card_cardoper_hs()
dwd_card_cardoper_hs = HiveOperator(
      task_id = "dwd_card_cardoper_hs",
      hql = dwd_card_cardoper_hs.sqldwd_card_cardoper_hs,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

# dwd业务条线快照表
dwd_card_ccdbiz = SQL_dwd_card_ccdbiz()
dwd_card_ccdbiz_cu = HiveOperator(
      task_id = "dwd_card_ccdbiz_cu",
      hql = dwd_card_ccdbiz.sqldwd_card_card_ccdbiz_cu,
      hive_cli_conn_id = 'hive',
      dag = dag
	)
#dwd业务条线账单快照表
dwd_card_ccdbiz_cyc = HiveOperator(
      task_id = "dwd_card_ccdbiz_cyc",
      hql = dwd_card_ccdbiz.sqldwd_card_card_ccdbiz_cyc,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

#dwd业务条线月末快照表
dwd_card_ccdbiz_mth = HiveOperator(
      task_id = "dwd_card_ccdbiz_mth",
      hql = dwd_card_ccdbiz.sqldwd_card_card_ccdbiz_mth,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

# dwd客户全量快照表
dwd_card_ccdcust = SQL_dwd_card_ccdcust()
dwd_card_ccdcust_cu = HiveOperator(
      task_id = "dwd_card_ccdcust_cu",
      hql = dwd_card_ccdcust.sqldwd_card_card_ccdcust_cu,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

# dwd客户全量快照表
dwd_card_ccdcust_mth = HiveOperator(
      task_id = "dwd_card_ccdcust_mth",
      hql = dwd_card_ccdcust.sqldwd_card_card_ccdcust_mth,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

# dwd卡历史表
dwd_card_other = SQL_dwd_card_other()
dwd_card_chgs_hs = HiveOperator(
      task_id = "dwd_card_chgs_hs",
      hql = dwd_card_other.sqldwd_card_chgs_hs,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

# dwd佣金流历史表
dwd_card_commtxn_hs = HiveOperator(
      task_id = "dwd_card_commtxn_hs",
      hql = dwd_card_other.sqldwd_card_commtxn_hs,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

#dwd分期
dwd_card_mpur_cu = HiveOperator(
      task_id = "dwd_card_mpur_cu",
      hql = dwd_card_other.sqldwd_card_mpur_cu,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

#变更历史
dwd_card_posnor_hs = HiveOperator(
      task_id = "dwd_card_posnor_hs",
      hql = dwd_card_other.sqldwd_card_posnor_hs,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

#账单
dwd_card_stmt_hs = HiveOperator(
      task_id = "dwd_card_stmt_hs",
      hql = dwd_card_other.sqldwd_card_stmt_hs,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

#交易历史
dwd_card_txn_hs = HiveOperator(
      task_id = "dwd_card_txn_hs",
      hql = dwd_card_other.sqldwd_card_txn_hs,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

#申请
dwd_card_appr_cu = HiveOperator(
      task_id = "dwd_card_appr_cu",
      hql = dwd_card_other.sqldwd_card_appr_cu,
      hive_cli_conn_id = 'hive',
      dag = dag
      )

#收入
dwd_card_income = SQL_dws_card_income()
dws_card_income_a_c = HiveOperator(
      task_id = "dws_card_income_a_c",
      hql = dwd_card_income.sqldws_card_income_a_c,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

dws_card_income_a_m = HiveOperator(
      task_id = "dws_card_income_a_m",
      hql = dwd_card_income.sqldws_card_income_a_m,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

dws_card_income_b_m = HiveOperator(
      task_id = "dws_card_income_b_m",
      hql = dwd_card_income.sqldws_card_income_b_m,
      hive_cli_conn_id = 'hive',
      dag = dag
	)
#dws分期
dwd_card_mpur = SQL_dws_card_mpur()
dws_card_mpur_a_d = HiveOperator(
      task_id = "dws_card_mpur_a_d",
      hql = dwd_card_mpur.sqldws_card_mpur_a_d,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

#dws交易
dwd_card_txn = SQL_dws_card_txn()
dws_card_txn_a_c = HiveOperator(
      task_id = "dws_card_txn_a_c",
      hql = dwd_card_txn.sqldws_card_txn_a_c,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

dws_card_txn_a_c_temp = HiveOperator(
      task_id = "dws_card_txn_a_c_temp",
      hql = dwd_card_txn.sqldws_card_txn_a_c_temp,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

dws_card_txn_a_d = HiveOperator(
      task_id = "dws_card_txn_a_d",
      hql = dwd_card_txn.sqldws_card_txn_a_d,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

dws_card_txn_a_m = HiveOperator(
      task_id = "dws_card_txn_a_m",
      hql = dwd_card_txn.sqldws_card_txn_a_m,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

dws_card_txn_b_m = HiveOperator(
      task_id = "dws_card_txn_b_m",
      hql = dwd_card_txn.sqldws_card_txn_b_m,
      hive_cli_conn_id = 'hive',
      dag = dag
	)


#运营风险报表
ads_bi_risk = SQL_ads_bi_risk()
ads_bi_risk_acct_c = HiveOperator(
      task_id = "ads_bi_risk_acct_c",
      hql = ads_bi_risk.sqlads_bi_risk_acct_c,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

ads_bi_risk_acct_m = HiveOperator(
      task_id = "ads_bi_risk_acct_m",
      hql = ads_bi_risk.sqlads_bi_risk_acct_m,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

ads_bi_risk_card_m = HiveOperator(
      task_id = "ads_bi_risk_card_m",
      hql = ads_bi_risk.sqlads_bi_risk_card_m,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

ads_bi_risk_card_vtg_d = HiveOperator(
      task_id = "ads_bi_risk_card_vtg_d",
      hql = ads_bi_risk.sqlads_bi_risk_card_vtg_d,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

ads_bi_risk_card_vtg_m = HiveOperator(
      task_id = "ads_bi_risk_card_vtg_m",
      hql = ads_bi_risk.sqlads_bi_risk_card_vtg_m,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

ads_bi_risk_grant_m = HiveOperator(
      task_id = "ads_bi_risk_grant_m",
      hql = ads_bi_risk.sqlads_bi_risk_grant_m,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

ads_bi_risk_scocnt_m = HiveOperator(
      task_id = "ads_bi_risk_scocnt_m",
      hql = ads_bi_risk.sqlads_bi_risk_scocnt_m,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

ads_bi_risk_scodis_m = HiveOperator(
      task_id = "ads_bi_risk_scodis_m",
      hql = ads_bi_risk.sqlads_bi_risk_scodis_m,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

ads_bi_risk_summary = HiveOperator(
      task_id = "ads_bi_risk_summary",
      hql = ads_bi_risk.sqlads_bi_risk_summary,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

#运营报表
ads_bi_umkt = SQL_ads_bi_umkt()
ads_bi_umkt_activate = HiveOperator(
      task_id = "ads_bi_umkt_activate",
      hql = ads_bi_umkt.sqlads_bi_umkt_activate,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

ads_bi_umkt_apma = HiveOperator(
      task_id = "ads_bi_umkt_apma",
      hql = ads_bi_umkt.sqlads_bi_umkt_apma,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

ads_bi_umkt_leftact = HiveOperator(
      task_id = "ads_bi_umkt_leftact",
      hql = ads_bi_umkt.sqlads_bi_umkt_apma,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

ads_bi_umkt_leftact_income = HiveOperator(
      task_id = "ads_bi_umkt_leftact_income",
      hql = ads_bi_umkt.sqlads_bi_umkt_leftact_income,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

ads_bi_umkt_leftact_vtg = HiveOperator(
      task_id = "ads_bi_umkt_leftact_vtg",
      hql = ads_bi_umkt.sqlads_bi_umkt_leftact_vtg,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

ads_bi_umkt_leftact_vtg_income = HiveOperator(
      task_id = "ads_bi_umkt_leftact_vtg_income",
      hql = ads_bi_umkt.sqlads_bi_umkt_leftact_vtg_income,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

ads_bi_umkt_userana = HiveOperator(
      task_id = "ads_bi_umkt_userana",
      hql = ads_bi_umkt.sqlads_bi_umkt_userana,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

#运营核心生产用
ads_umkt = SQL_ads_umkt()
ads_umkt_acct = HiveOperator(
      task_id = "ads_umkt_acct",
      hql = ads_umkt.sqlads_umkt_acct,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

ads_umkt_ccdcust = HiveOperator(
      task_id = "ads_umkt_ccdcust",
      hql = ads_umkt.sqlads_umkt_ccdcust,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

ads_umkt_ccdcust_tag = HiveOperator(
      task_id = "ads_umkt_ccdcust_tag",
      hql = ads_umkt.sqlads_umkt_ccdcust_tag,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

ads_umkt_chgs = HiveOperator(
      task_id = "ads_umkt_chgs",
      hql = ads_umkt.sqlads_umkt_chgs,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

ads_umkt_inapprcustr = HiveOperator(
      task_id = "ads_umkt_inapprcustr",
      hql = ads_umkt.sqlads_umkt_inapprcustr,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

ads_umkt_limit = HiveOperator(
      task_id = "ads_umkt_limit",
      hql = ads_umkt.sqlads_umkt_limit,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

ads_umkt_merchant = HiveOperator(
      task_id = "ads_umkt_merchant",
      hql = ads_umkt.sqlads_umkt_merchant,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

ads_umkt_mpur = HiveOperator(
      task_id = "ads_umkt_mpur",
      hql = ads_umkt.sqlads_umkt_mpur,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

#ads_umkt_para_card_accttype = HiveOperator(
#      task_id = "ads_umkt_para_card_accttype",
#      hql = ads_umkt.sqlads_umkt_para_card_accttype,
#      hive_cli_conn_id = 'hive',
#      dag = dag
#	)

#ads_umkt_para_card_cardproduct = HiveOperator(
#      task_id = "ads_umkt_para_card_cardproduct",
#      hql = ads_umkt.sqlads_umkt_para_card_cardproduct,
#      hive_cli_conn_id = 'hive',
#      dag = dag
#	)

ads_umkt_stmt = HiveOperator(
      task_id = "ads_umkt_stmt",
      hql = ads_umkt.sqlads_umkt_stmt,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

ads_umkt_tagcal = HiveOperator(
      task_id = "ads_umkt_tagcal",
      hql = ads_umkt.sqlads_umkt_tagcal,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

ads_umkt_txn = HiveOperator(
      task_id = "ads_umkt_txn",
      hql = ads_umkt.sqlads_umkt_txn,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

ads_umkt_txnchl = HiveOperator(
      task_id = "ads_umkt_txnchl",
      hql = ads_umkt.sqlads_umkt_txnchl,
      hive_cli_conn_id = 'hive',
      dag = dag
	)

#摘要
ads_bi_risk_acct_c_zy = HiveOperator(
      task_id = "ads_bi_risk_acct_c_zy",
      hql = ads_bi_risk.sqlads_bi_risk_acct_c_zy,
      hive_cli_conn_id = 'hive',
      dag = dag
	)


ods_data_check = BashOperator(
    task_id='ods_data_check',
    bash_command='date',
    dag=dag)


#设置依赖关系
#先跑数据层，并在跑批前检查当天ods跑数情况
apt_card_txn_cu.set_upstream(ods_data_check)
apt_card_stmt_cu.set_upstream(ods_data_check)
apt_card_mpur_cu.set_upstream(ods_data_check)
apt_card_jorj_cu.set_upstream(ods_data_check)
apt_card_commtxn_cu.set_upstream(ods_data_check)
apt_card_cnta_cu.set_upstream(ods_data_check)
apt_card_chgs_cu.set_upstream(ods_data_check)
apt_card_ccdcust_cu.set_upstream(ods_data_check)
apt_card_card_cu.set_upstream(ods_data_check)
apt_card_appr_cu.set_upstream(ods_data_check)
apt_card_apma_cu.set_upstream(ods_data_check)
apt_card_acct_cu.set_upstream(ods_data_check)

#stg层
stg_card_acct_cu.set_upstream(apt_card_acct_cu)
stg_card_acct_cu.set_upstream(dwd_card_stmt_hs)
stg_card_acct_cu.set_upstream(dws_card_mpur_a_d)
stg_card_acct_cu.set_upstream(dws_card_txn_a_c)
stg_card_acct_cu.set_upstream(dws_card_txn_a_c_temp)
stg_card_acct_cu.set_upstream(dws_card_txn_a_d)
#stg_card_acct_cu.set_upstream(stg_card_acct_cu)
stg_card_acct_cu.set_upstream(stg_card_card_cu)

stg_card_card_cu.set_upstream(apt_card_acct_cu)
stg_card_card_cu.set_upstream(apt_card_apma_cu)
stg_card_card_cu.set_upstream(apt_card_card_cu)

stg_card_ccdbiz_cu.set_upstream(stg_card_acct_cu)

stg_card_mpur_cu.set_upstream(apt_card_acct_cu)
stg_card_mpur_cu.set_upstream(apt_card_mpur_cu)

#dwd层
dwd_card_stmt_hs.set_upstream(apt_card_stmt_cu)

dwd_card_commtxn_hs.set_upstream(apt_card_commtxn_cu)
dwd_card_commtxn_hs.set_upstream(stg_card_card_cu)

dwd_card_txn_hs.set_upstream(apt_card_txn_cu)


dwd_card_cardoper_hs.set_upstream(apt_card_card_cu)
dwd_card_cardoper_hs.set_upstream(apt_card_cardoper_cu)

dwd_card_ccdcust_cu.set_upstream(apt_card_card_cu)
dwd_card_ccdcust_cu.set_upstream(apt_card_ccdcust_cu)
dwd_card_ccdcust_cu.set_upstream(dwd_card_cardoper_hs)
dwd_card_ccdcust_cu.set_upstream(stg_card_acct_cu)
dwd_card_ccdcust_cu.set_upstream(stg_card_card_cu)


#dwd_card_acctbal_hs.set_upstream(dwd_card_acctbal_hs)
dwd_card_acctbal_hs.set_upstream(stg_card_acct_cu)

dwd_card_acct_cu.set_upstream(stg_card_acct_cu)

dwd_card_acct_cyc.set_upstream(stg_card_acct_cu)

dwd_card_mpur_cu.set_upstream(stg_card_mpur_cu)

dwd_card_card_cu.set_upstream(stg_card_card_cu)

dwd_card_posnor_hs.set_upstream(dwd_card_txn_hs)

dwd_card_chgs_hs.set_upstream(apt_card_chgs_cu)

dwd_card_apma_cu.set_upstream(apt_card_acct_cu)
dwd_card_apma_cu.set_upstream(apt_card_apma_cu)
dwd_card_apma_cu.set_upstream(stg_card_acct_cu)

dwd_card_ccdbiz_cyc.set_upstream(dwd_card_stmt_hs)
dwd_card_ccdbiz_cyc.set_upstream(stg_card_acct_cu)

dwd_card_ccdbiz_cu.set_upstream(stg_card_ccdbiz_cu)

dwd_card_ccdbiz_mth.set_upstream(stg_card_ccdbiz_cu)

dwd_card_ccdcust_mth.set_upstream(dwd_card_ccdcust_cu)

dwd_card_acct_mth.set_upstream(stg_card_acct_cu)

dwd_card_card_mth.set_upstream(stg_card_card_cu)

#dws层
dws_card_income_b_m.set_upstream(apt_card_acct_cu)
dws_card_income_b_m.set_upstream(dwd_card_commtxn_hs)
dws_card_income_b_m.set_upstream(dwd_card_stmt_hs)
dws_card_income_b_m.set_upstream(dwd_card_txn_hs)
#dws_card_income_b_m.set_upstream(dws_card_income_b_m)
dws_card_income_b_m.set_upstream(stg_card_acct_cu)
dws_card_income_b_m.set_upstream(stg_card_ccdbiz_cu)

dws_card_income_a_c.set_upstream(apt_card_acct_cu)
dws_card_income_a_c.set_upstream(dwd_card_commtxn_hs)
dws_card_income_a_c.set_upstream(dwd_card_stmt_hs)
dws_card_income_a_c.set_upstream(dwd_card_txn_hs)
#dws_card_income_a_c.set_upstream(dws_card_income_a_c)

dws_card_txn_a_c_temp.set_upstream(apt_card_acct_cu)
dws_card_txn_a_c_temp.set_upstream(dwd_card_txn_hs)

dws_card_income_a_m.set_upstream(apt_card_acct_cu)
dws_card_income_a_m.set_upstream(dwd_card_commtxn_hs)
dws_card_income_a_m.set_upstream(dwd_card_stmt_hs)
dws_card_income_a_m.set_upstream(dwd_card_txn_hs)
#dws_card_income_a_m.set_upstream(dws_card_income_a_m)

dws_card_txn_b_m.set_upstream(dwd_card_txn_hs)
dws_card_txn_b_m.set_upstream(stg_card_acct_cu)
dws_card_txn_b_m.set_upstream(stg_card_ccdbiz_cu)

dws_card_txn_a_c.set_upstream(dws_card_txn_a_c_temp)

dws_card_txn_a_m.set_upstream(apt_card_acct_cu)
dws_card_txn_a_m.set_upstream(dwd_card_txn_hs)

dws_card_mpur_a_d.set_upstream(apt_card_acct_cu)
dws_card_mpur_a_d.set_upstream(stg_card_mpur_cu)

dws_card_txn_a_d.set_upstream(apt_card_acct_cu)
dws_card_txn_a_d.set_upstream(dwd_card_txn_hs)

#ad_umkt
ads_umkt_acct.set_upstream(dwd_card_acct_cu)
ads_umkt_acct.set_upstream(dwd_card_card_cu)

ads_umkt_ccdcust.set_upstream(dwd_card_ccdcust_cu)

ads_umkt_ccdcust_tag.set_upstream(ads_umkt_inapprcustr)
ads_umkt_ccdcust_tag.set_upstream(apt_card_ccdcust_cu)
ads_umkt_ccdcust_tag.set_upstream(dwd_card_card_cu)
ads_umkt_ccdcust_tag.set_upstream(dwd_card_acct_cu)
ads_umkt_ccdcust_tag.set_upstream(dwd_card_ccdcust_cu)

ads_umkt_chgs.set_upstream(dwd_card_chgs_hs)
ads_umkt_chgs.set_upstream(dwd_card_acct_cu)

ads_umkt_inapprcustr.set_upstream(dwd_card_appr_cu)

ads_umkt_limit.set_upstream(dwd_card_acctbal_hs)
ads_umkt_limit.set_upstream(dwd_card_acct_cu)

ads_umkt_merchant.set_upstream(ads_umkt_txn)

ads_umkt_mpur.set_upstream(dwd_card_card_cu)
ads_umkt_mpur.set_upstream(dwd_card_mpur_cu)

ads_umkt_stmt.set_upstream(dwd_card_acct_cyc)
ads_umkt_stmt.set_upstream(dwd_card_stmt_hs)

ads_umkt_tagcal.set_upstream(ads_umkt_ccdcust_tag)

ads_umkt_txn.set_upstream(dwd_card_txn_hs)
ads_umkt_txn.set_upstream(dwd_card_posnor_hs)
ads_umkt_txn.set_upstream(dwd_card_acct_cu)
ads_umkt_txn.set_upstream(dwd_card_card_cu)

ads_umkt_txnchl.set_upstream(ads_umkt_txn)


#ads_bi_umkt
ads_bi_umkt_activate.set_upstream(dwd_card_apma_cu)
ads_bi_umkt_activate.set_upstream(dwd_card_card_cu)

ads_bi_umkt_apma.set_upstream(dwd_card_apma_cu)

ads_bi_umkt_leftact.set_upstream(dwd_card_acct_cyc)
ads_bi_umkt_leftact.set_upstream(dwd_card_ccdbiz_cyc)
ads_bi_umkt_leftact.set_upstream(dws_card_income_b_m)
ads_bi_umkt_leftact.set_upstream(dws_card_txn_b_m)

ads_bi_umkt_leftact_income.set_upstream(dwd_card_ccdbiz_cu)
ads_bi_umkt_leftact_income.set_upstream(dwd_card_ccdbiz_cyc)
ads_bi_umkt_leftact_income.set_upstream(dws_card_income_b_m)

ads_bi_umkt_leftact_vtg.set_upstream(dwd_card_acct_cyc)
ads_bi_umkt_leftact_vtg.set_upstream(dwd_card_ccdbiz_cu)
ads_bi_umkt_leftact_vtg.set_upstream(dwd_card_ccdbiz_cyc)
ads_bi_umkt_leftact_vtg.set_upstream(dws_card_txn_b_m)
ads_bi_umkt_leftact_vtg.set_upstream(dws_card_income_b_m)


ads_bi_umkt_leftact_vtg_income.set_upstream(dwd_card_ccdbiz_cu)
ads_bi_umkt_leftact_vtg_income.set_upstream(dwd_card_ccdbiz_cyc)
ads_bi_umkt_leftact_vtg_income.set_upstream(dws_card_income_b_m)

ads_bi_umkt_userana.set_upstream(dwd_card_ccdbiz_mth)
ads_bi_umkt_userana.set_upstream(dwd_card_ccdcust_mth)

#ads_bi_risk
ads_bi_risk_acct_c.set_upstream(dwd_card_acct_cyc)
ads_bi_risk_acct_c.set_upstream(dws_card_income_a_c)
ads_bi_risk_acct_c.set_upstream(dws_card_txn_a_c)

ads_bi_risk_acct_m.set_upstream(dws_card_txn_a_m)
ads_bi_risk_acct_m.set_upstream(dws_card_income_a_m)
ads_bi_risk_acct_m.set_upstream(dwd_card_acct_mth)

ads_bi_risk_card_m.set_upstream(dwd_card_card_mth)

ads_bi_risk_card_vtg_d.set_upstream(dwd_card_card_cu)
ads_bi_risk_card_vtg_d.set_upstream(dwd_card_apma_cu)

ads_bi_risk_card_vtg_m.set_upstream(dwd_card_card_cu)
ads_bi_risk_card_vtg_m.set_upstream(dwd_card_apma_cu)

ads_bi_risk_grant_m.set_upstream(dwd_card_card_cu)
ads_bi_risk_grant_m.set_upstream(dwd_card_apma_cu)

ads_bi_risk_scocnt_m.set_upstream(apt_pboc_queryinfo)
ads_bi_risk_scocnt_m.set_upstream(apt_pboc_idvcrrprtnum)
ads_bi_risk_scocnt_m.set_upstream(dwd_card_acct_cu)

ads_bi_risk_scodis_m.set_upstream(apt_pboc_queryinfo)
ads_bi_risk_scodis_m.set_upstream(apt_pboc_idvcrrprtnum)
ads_bi_risk_scodis_m.set_upstream(dwd_card_acct_cu)

ads_bi_risk_summary.set_upstream(dwd_card_card_cu)
ads_bi_risk_summary.set_upstream(dwd_card_acct_cyc)
ads_bi_risk_summary.set_upstream(dws_card_txn_a_m)
ads_bi_risk_summary.set_upstream(dws_card_income_a_m)
ads_bi_risk_summary.set_upstream(dwd_card_acct_mth)
ads_bi_risk_summary.set_upstream(dwd_card_card_mth)
ads_bi_risk_summary.set_upstream(dwd_card_acct_cu)

#摘要
ads_bi_risk_acct_c_zy.set_upstream(dwd_card_acct_cyc)
ads_bi_risk_acct_c_zy.set_upstream(dwd_card_stmt_hs)
ads_bi_risk_acct_c_zy.set_upstream(dws_card_income_a_c)
ads_bi_risk_acct_c_zy.set_upstream(dws_card_txn_a_c)