##ClickHouse之配置文件

    1.总配置文件分为：config.xml users.xml
    
    2.可为单个用户进行配置 例：/etc/clickhouse-server/users.d/alice.xml
    
    3.compression：MergeTree引擎表的数据压缩设置。配置模板如：
      
        <compression incl="clickhouse_compression">  --指定incl
            <case>
                <min_part_size>10000000000</min_part_size> --数据部分的最小大小
                <min_part_size_ratio>0.01</min_part_size_ratio> --数据部分大小与表大小的比率
                <method>zstd</method> --压缩算法，zstd和lz4
            </case>
        </compression>
        
    4.listen_host：限制来源主机的请求， 如果要服务器回答所有请求，请指定“::” ：
      
        <listen_host> :: 1 </ listen_host>
        <listen_host> 127.0.0.1 </ listen_host>
        
    5.optimize_throw_if_noop：如果OPTIMIZE查询未执行合并，则启用或禁用引发异常。默认0，可选0：禁用引发异常；1：启用引发异常
    
    6.