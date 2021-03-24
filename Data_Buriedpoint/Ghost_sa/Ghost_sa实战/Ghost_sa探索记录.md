1.TiDB重新通过docker进行部署

2.发现了一处BUG（在配置文件中设置use_kafka=Flase但在主程序中不生效），已于作者交流后修改trigger.py，添加如下内容：

	    from configs import admin
	if admin.use_kafka is True:
	    from component.kafka_op import get_message_from_kafka_independent_listener
    
3.安装flask-cors、kafka-python模块

	pip3 install -U flask-cors
	pip3 install kafka-python

4.测试

	随便找个神策的SDK（或者使用JS进行测试），接受地址填那个sa.gif，数据库里就能看到；project是你setup时候的名字；remark写了就是你写的remark，不写的话会默认填个normal。
	--http://172.16.0.52:8000/sa.gif?project=my_app_test&remark=dev1
  
5.备注

	5.1 建议remark也指定上。尤其是开发最好在引入的时候就做好生产，测试，开发环境的判断。这样取数据的时候方便。
	5.2 project_name尽量短。名字越长，占用GET请求的位数就越多。后续报特别复杂的埋点的时候就越有可能因为长度限制不够位。
	5.3 可以用gunicorn或者其他服务方式跑。flask直接对外也不是不行，可以自己建服务。
	5.4 换成mysql，又不使用kafka的话。性能会很差。
	5.5 kafka可以和鬼策一台服务器,4核8G就足够鬼策和kafka抗住500~600万一天
	5.6 configs/kafka.py里的bootstrap_servers=['dxbigdata103:9092']，不能是IP地址，应设置为hostname
