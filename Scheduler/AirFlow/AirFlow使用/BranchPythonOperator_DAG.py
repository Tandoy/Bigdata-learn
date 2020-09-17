#依赖分支判断测试
# -*- coding: utf-8 -*-
 
import airflow
from airflow import DAG
from airflow.operators.bash_operator import BashOperator
from airflow.operators.python_operator import PythonOperator
from airflow.operators.python_operator import BranchPythonOperator
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
    'BranchPythonOperator_dag', # dag_id
    default_args=default_args, # 指定默认参数
    description='BranchPythonOperator_dag',
    schedule_interval=timedelta(days=1)) # 执行周期，依次是分，时，天，月，年，此处表示每个整点执行;此处表示每天执行一次
 
#-------------------------------------------------------------------------------

# first operator 通过BashOperator定义执行bash命令的任务
date_operator = BashOperator( 
    task_id='date_task',
    bash_command='date',
    dag=dag) # 指定归属的dag

def print_brancha():
    return 'Hello brancha'
    
 # second operator
 
branch_a = PythonOperator(
    task_id='branch_a',
    depends_on_past=False,
    python_callable=print_brancha,
    dag=dag)
 
#-------------------------------------------------------------------------------
# third operator 通过PythonOperator定义执行python函数的任务
def print_branchb():
    return 'Hello branchb!'
 
branch_b = PythonOperator(
    task_id='branch_b',
    python_callable=print_branchb,
    dag=dag) # 指定归属的dag

def print_branchc():
    return 'Hello branchc!'
 
branch_c = PythonOperator(
    task_id='branch_c',
    python_callable=print_branchc,
    dag=dag) # 指定归属的dag
#-------------------------------------------------------------------------------
def decide_which_path():
      if 1 > 1:
          return "branch_a"
      else:
          return "branch_b"
  
  
branch_task = BranchPythonOperator(
      task_id='run_this_first',
      python_callable=decide_which_path,
      trigger_rule="all_done",
      dag=dag)
#-------------------------------------------------------------------------------
# dependencies
branch_task.set_downstream(branch_a) #适配层以及中间层、应用层都依赖于branch_a
branch_task.set_downstream(branch_b)
branch_a.set_downstream(branch_c)
