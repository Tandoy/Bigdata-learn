修改：airflow.cfg文件。

1.default_timezone = Asia/Shanghai ##这里修改的是schedule的调度时间，即在编写调度时间时可以直接写北京时间。

2.用户认证
  2.1 安装passsword组件
      pip install "apache-airflow[password]"
  2.2 修改 airflow.cfg
      [webserver]
      authenticate = True
      auth_backend = airflow.contrib.auth.backends.password_auth
  2.3 在python环境中执行如下代码以添加账户：
      import airflow  
      from airflow import models, settings  
      from airflow.contrib.auth.backends.password_auth import PasswordUser  
      user = PasswordUser(models.User())  
      user.username = 'admin'  # 用户名
      user.email = 'emailExample@163.com' # 用户邮箱  
      user.password = 'password'   # 用户密码
      session = settings.Session()  
      session.add(user)  
      session.commit()  
      session.close()  
      exit() 
  
3.设置Executor
  executor = LocalExecutor: 生产上大多为分布式调度
  
  SequentialExecutor：表示单进程顺序执行，通常只用于测试
  
  LocalExecutor：表示多进程本地执行，它用python的多进程库从而达到多进程跑任务的效果。
  
  CeleryExecutor：表示使用celery作为执行器，只要配置了celery，就可以分布式地多机跑任务，一般用于生产环境。
  
  DaskExecutor ：动态任务调度，主要用于数据分析
  
  KubernetesExecutor：airflow 1.10.0引入，为每个任务实例创建一个pod，结合k8s使用。
