# -*- coding: utf-8 -*-
 
import airflow
from airflow import DAG
from airflow.operators.bash_operator import BashOperator
from airflow.operators.python_operator import PythonOperator
from datetime import timedelta
 
#-------------------------------------------------------------------------------
# these args will get passed on to each operator
# you can override them on a per-task basis during operator initialization
 
default_args = {
    'owner': 'tzhi',   # 拥有者名称
    'depends_on_past': False, # 是否依赖上一个自己的执行状态
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
    'example_hello_world_dag', # dag_id
    default_args=default_args, # 指定默认参数
    description='my first DAG',
    schedule_interval=timedelta(days=1)) # 执行周期，依次是分，时，天，月，年，此处表示每个整点执行;此处表示每天执行一次
 
#-------------------------------------------------------------------------------

# first operator 通过BashOperator定义执行bash命令的任务
date_operator = BashOperator( 
    task_id='date_task',
    bash_command='date',
    dag=dag) # 指定归属的dag
    
 # second operator
 
sleep_operator = BashOperator(
    task_id='sleep_task',
    depends_on_past=False,
    bash_command='sleep 5',
    dag=dag)
 
#-------------------------------------------------------------------------------
# third operator 通过PythonOperator定义执行python函数的任务
def print_hello():
    return 'Hello world!'
 
hello_operator = PythonOperator(
    task_id='hello_task',
    python_callable=print_hello,
    dag=dag) # 指定归属的dag
 
#-------------------------------------------------------------------------------
# dependencies
sleep_operator.set_upstream(date_operator) 
hello_operator.set_upstream(date_operator)
# sleep_operator依赖于date_operator;等价于 date_operator.set_downstream(sleep_operator);同时等价于 dag.set_dependency('date_operator', 'sleep_operator')# 表示t2这个任务只有在t1这个任务执行成功时才执行，# 或者
date_operator >> sleep_operator
