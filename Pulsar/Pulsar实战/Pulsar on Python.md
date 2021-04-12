##Pulsar on Python

###前提Pulsar已经启动

    1.直接从 PyPI 安装 Pulsar 的 Python 客户端库
    
        pip install pulsar-client
        
    2.Consume 一条消息
        创建 consumer 并订阅 topic：
            
            import pulsar
            
            client = pulsar.Client('pulsar://localhost:6650')
            consumer = client.subscribe('my-topic',
                                        subscription_name='my-sub')
            
            while True:
                msg = consumer.receive()
                print("Received message: '%s'" % msg.data())
                consumer.acknowledge(msg)
            
            client.close()
            
    3.Produce 一条消息
        启动 producer，发送测试消息：
        
            import pulsar
            
            client = pulsar.Client('pulsar://localhost:6650')
            producer = client.create_producer('my-topic')
            
            for i in range(10):
                producer.send(('hello-pulsar-%d' % i).encode('utf-8'))
            
            client.close()
    
    4.获取 topic 数据
    
        curl http://localhost:8080/admin/v2/persistent/public/default/my-topic/stats | python -m json.tool
        
        