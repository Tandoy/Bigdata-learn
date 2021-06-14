## flume的采集配置

    a1.sources = r1
    a1.channels = c1
    a1.sinks = k1
    #source config
    a1.sources.r1.channels = c1
    a1.sources.r1.type = org.apache.flume.source.kafka.KafkaSource --从kafka收集信息
    a1.sources.r1.kafka.bootstrap.servers = doitedu01:9092,doitedu02:9092,doitedu03:9092  ##从那几台机器采取kafka信息
    a1.sources.r1.kafka.consumer.group.id = doit --kafka组id
    a1.sources.r1.kafka.topics = wxapp_log,app_log --kafka的topic,可以写多个
    a1.sources.r1.setTopicHeader = true  
    a1.sources.r1.topicHeader = topic
    # channel config
    a1.channels.c1.type = file
    # sink config
    a1.sinks.k1.channel = c1
    a1.sinks.k1.type = hdfs
    a1.sinks.k1.hdfs.path = hdfs://dxbigdata101:8020/event_log/%{topic}/%Y-%m-%d/ --%{topic} ##可以采取通配符 
    a1.sources.r1.topicHeader = topic  ##接受消息使用topic 就可以用%{topic}
    a1.sinks.k1.hdfs.filePrefix = event_
    a1.sinks.k1.hdfs.fileSuffix = .log
    a1.sinks.k1.hdfs.rollInterval = 0
    a1.sinks.k1.hdfs.rollCount = 0
    a1.sinks.k1.hdfs.rollSize = 134217728
    a1.sinks.k1.hdfs.round = true
    a1.sinks.k1.hdfs.roundValue = 10
    a1.sinks.k1.hdfs.roundUnit = minute
    a1.sinks.k1.hdfs.fileType = DataStream
