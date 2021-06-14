## Yarn生产调优手册-生产经验

1.常用的调优参数
```text
1）调优参数列表
    （1）Resourcemanager 相关
        yarn.resourcemanager.scheduler.client.thread-count ResourceManager 处理调度器请求的线程数量
        yarn.resourcemanager.scheduler.class 配置调度器
    （2）Nodemanager 相关
        yarn.nodemanager.resource.memory-mb NodeManager 使用内存数
        yarn.nodemanager.resource.system-reserved-memory-mb NodeManager 为系统保留多少内存，和上一个参数二者取一即可
        yarn.nodemanager.resource.cpu-vcores NodeManager 使用 CPU 核数
        yarn.nodemanager.resource.count-logical-processors-as-cores 是否将虚拟核数当作 CPU 核数
        yarn.nodemanager.resource.pcores-vcores-multiplier 虚拟核数和物理核数乘数，例如：4 核 8 线程，该参数就应设为 2
        yarn.nodemanager.resource.detect-hardware-capabilities 是否让 yarn 自己检测硬件进行配置
        yarn.nodemanager.pmem-check-enabled 是否开启物理内存检查限制 container
        yarn.nodemanager.vmem-check-enabled 是否开启虚拟内存检查限制 container
        yarn.nodemanager.vmem-pmem-ratio 虚拟内存物理内存比例
    （3）Container 容器相关
        yarn.scheduler.minimum-allocation-mb 容器最小内存
        yarn.scheduler.maximum-allocation-mb 容器最大内存
        yarn.scheduler.minimum-allocation-vcores 容器最小核数
        yarn.scheduler.maximum-allocation-vcores 容器最大核数
```