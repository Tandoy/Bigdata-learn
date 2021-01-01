1.DSS多次重复安装后报错:TooManyResultsException:Expected on result

     删除数据库中表linkis_user、dss_user、linkis_application中的重复记录
     
2.访问前端出错
  
    Unexpected token { in JSON at position 4
    检查前端配置文件中linkis gateway的url配置
    
3.visualis可视化服务访问报错

  a) 确保visualis-server已经启动。
  
  b) 检查visualis-server安装目录下的application.yml配置，确保以下配置准确无误
  
    url: http://0.0.0.0:0000/dss/visualis  此处url中的IP和端口必须保持与DSS前端Nginx访问的IP地址和端口一致
    access:
      address: 0.0.0.0 #frontend address，此处保持与DSS前端Nginx访问IP地址一致
      port: 0000#frontend port，此处保持与DSS前端Nginx访问端口一致
      
  c) 确保数据库表dss_application中 visualis记录行，访问地址与DSS前端Nginx访问IP地址和端口一致。
  
  d) 访问visualis出现404错误，确保Nginx配置文件中关于visualis的访问路径配置正确。
  
              location /dss/visualis {
              root   /data/DSSFront; # 示例visualis前端静态文件目录
              autoindex on;
              }
              location / {
              root   /data/DSSFront/dist; # 示例DSS前端静态文件目录
              index  index.html index.html;
              }     
              
4. 其他问题详情见：https://github.com/WeBankFinTech/DataSphereStudio/blob/master/docs/zh_CN/ch1/DSS%E5%AE%89%E8%A3%85%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98%E5%88%97%E8%A1%A8.md