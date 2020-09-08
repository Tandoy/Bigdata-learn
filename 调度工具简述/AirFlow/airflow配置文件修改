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
  executor = LocalExecutor  ##生产上大多为分布式调度
