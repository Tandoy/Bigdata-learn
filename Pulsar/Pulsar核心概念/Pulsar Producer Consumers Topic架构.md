## Pulsar Producer Consumers架构

### 一、Producer

1.处理一个 producer 和一个订阅 consumer 的分块消息

![image](https://github.com/Tandoy/Bigdata-learn/blob/master/Pulsar/images/chunking-01.png)

2.多个生产者和一个生产者处理块消息

![image](https://github.com/Tandoy/Bigdata-learn/blob/master/Pulsar/images/chunking-02.png)


### 二、Consumers

    1.确认：
        当消费者成功的消费了一条消息，这个消费者会发送一个确认信息给broker。 
        这个消息时是永久保存的，只有在收到订阅者消费成功的消息确认后才会被删除。 如果希望消息被 Consumer 确认后仍然保留下来，可配置 消息保留策略实现
        
    2.取消确认：
        当消费者在某个时间没有成功的消费某条消息，消费者想重新消费到这条消息，这个消费者可以发送一条取消确认消息到 broker，broker 会将这条消息重新发给消费者。 
        消息取消确认也有单条取消模式和累积取消模式 ，这依赖于消费者使用的订阅模式。
        在独占消费模式和灾备订阅模式中，消费者仅仅只能对收到的最后一条消息进行取消确认。
    
    3.确认超时
        如果消息没有被成功消费，你想去让 broker 自动重新交付这个消息， 你可以采用未确认消息自动重新交付机制。 
        客户端会跟踪 超时 时间范围内所有未确认的消息。 并且在指定超时时间后会发送一个 重发未确认的消息 请求到 broker。
        
### 三、Topic
    
    Pulsar 支持两种主题类型：持久化和非持久化。 主题默认是持久化类型。
    
### 四、消息保留和过期

    ·立即删除所有已经被cunsumer确认过的的消息
    ·以消息backlog的形式，持久保存所有的未被确认消息