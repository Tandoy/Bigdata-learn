private KafkaProducer(ProducerConfig config, Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        try {
            log.trace("Starting the Kafka producer");
            Map<String, Object> userProvidedConfigs = config.originals();
            this.producerConfig = config;
            this.time = new SystemTime();
            //连接号
            clientId = config.getString(ProducerConfig.CLIENT_ID_CONFIG);
            if (clientId.length() <= 0)
                //用户设置取用户设置，否则默认kafka生成唯一id
                Map<String, String> metricTags = new LinkedHashMap<String, String>();
            /**
             * 源码学习中metric相关暂不研究
             */
            metricTags.put("client-id", clientId);
            MetricConfig metricConfig = new MetricConfig().samples(config.getInt(ProducerConfig.METRICS_NUM_SAMPLES_CONFIG))
                    .timeWindow(config.getLong(ProducerConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG), TimeUnit.MILLISECONDS)
                    .tags(metricTags);
            List<MetricsReporter> reporters = config.getConfiguredInstances(ProducerConfig.METRIC_REPORTER_CLASSES_CONFIG,
                    MetricsReporter.class);
            reporters.add(new JmxReporter(JMX_PREFIX));
            this.metrics = new Metrics(metricConfig, reporters, time);
            //获取分区规则类
            this.partitioner = config.getConfiguredInstance(ProducerConfig.PARTITIONER_CLASS_CONFIG, Partitioner.class);
            //生产者与服务集群重试间隔，默认100ms
            long retryBackoffMs = config.getLong(ProducerConfig.RETRY_BACKOFF_MS_CONFIG);
            //设置key-value序列化器
            if (keySerializer == null) {
                this.keySerializer = config.getConfiguredInstance(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                        Serializer.class);
                this.keySerializer.configure(config.originals(), true);
            } else {
                config.ignore(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG);
                this.keySerializer = keySerializer;
            }
            if (valueSerializer == null) {
                this.valueSerializer = config.getConfiguredInstance(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                        Serializer.class);
                this.valueSerializer.configure(config.originals(), false);
            } else {
                config.ignore(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG);
                this.valueSerializer = valueSerializer;
            }

            // load interceptors and make sure they get clientId
            userProvidedConfigs.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
            //生产者发送消息先经过拦截器（类似消息过滤器）；实际开发很少使用
            List<ProducerInterceptor<K, V>> interceptorList = (List) (new ProducerConfig(userProvidedConfigs)).getConfiguredInstances(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG,
                    ProducerInterceptor.class);
            this.interceptors = interceptorList.isEmpty() ? null : new ProducerInterceptors<>(interceptorList);

            ClusterResourceListeners clusterResourceListeners = configureClusterResourceListeners(keySerializer, valueSerializer, interceptorList, reporters);
            //Metadata：从kafka服务端获取的元数据
            //METADATA_MAX_AGE_ms：生产者与服务端获取元数据间隔，默认5min
            this.metadata = new Metadata(retryBackoffMs, config.getLong(ProducerConfig.METADATA_MAX_AGE_CONFIG), true, clusterResourceListeners);
            //MAX_REQUEST_SIZE：生产者往服务端发送消息规定大小，默认1M；但实际生产中多为10M
            this.maxRequestSize = config.getInt(ProducerConfig.MAX_REQUEST_SIZE_CONFIG);
            //BUFFER_MEMORY_SIZE：RecordAcuumulator缓冲区大小，默认32M
            this.totalMemorySize = config.getLong(ProducerConfig.BUFFER_MEMORY_CONFIG);
            //kafka支持设置数据压缩格式，默认不压缩；若想提高系统吞吐量，可自行设置
            this.compressionType = CompressionType.forName(config.getString(ProducerConfig.COMPRESSION_TYPE_CONFIG));
            /* check for user defined settings.
             * If the BLOCK_ON_BUFFER_FULL is set to true,we do not honor METADATA_FETCH_TIMEOUT_CONFIG.
             * This should be removed with release 0.9 when the deprecated configs are removed.
             */
            if (userProvidedConfigs.containsKey(ProducerConfig.BLOCK_ON_BUFFER_FULL_CONFIG)) {
                log.warn(ProducerConfig.BLOCK_ON_BUFFER_FULL_CONFIG + " config is deprecated and will be removed soon. " +
                        "Please use " + ProducerConfig.MAX_BLOCK_MS_CONFIG);
                boolean blockOnBufferFull = config.getBoolean(ProducerConfig.BLOCK_ON_BUFFER_FULL_CONFIG);
                if (blockOnBufferFull) {
                    this.maxBlockTimeMs = Long.MAX_VALUE;
                } else if (userProvidedConfigs.containsKey(ProducerConfig.METADATA_FETCH_TIMEOUT_CONFIG)) {
                    log.warn(ProducerConfig.METADATA_FETCH_TIMEOUT_CONFIG + " config is deprecated and will be removed soon. " +
                            "Please use " + ProducerConfig.MAX_BLOCK_MS_CONFIG);
                    this.maxBlockTimeMs = config.getLong(ProducerConfig.METADATA_FETCH_TIMEOUT_CONFIG);
                } else {
                    this.maxBlockTimeMs = config.getLong(ProducerConfig.MAX_BLOCK_MS_CONFIG);
                }
            } else if (userProvidedConfigs.containsKey(ProducerConfig.METADATA_FETCH_TIMEOUT_CONFIG)) {
                log.warn(ProducerConfig.METADATA_FETCH_TIMEOUT_CONFIG + " config is deprecated and will be removed soon. " +
                        "Please use " + ProducerConfig.MAX_BLOCK_MS_CONFIG);
                this.maxBlockTimeMs = config.getLong(ProducerConfig.METADATA_FETCH_TIMEOUT_CONFIG);
            } else {
                this.maxBlockTimeMs = config.getLong(ProducerConfig.MAX_BLOCK_MS_CONFIG);
            }

            /* check for user defined settings.
             * If the TIME_OUT config is set use that for request timeout.
             * This should be removed with release 0.9
             */
            if (userProvidedConfigs.containsKey(ProducerConfig.TIMEOUT_CONFIG)) {
                log.warn(ProducerConfig.TIMEOUT_CONFIG + " config is deprecated and will be removed soon. Please use " +
                        ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG);
                this.requestTimeoutMs = config.getInt(ProducerConfig.TIMEOUT_CONFIG);
            } else {
                this.requestTimeoutMs = config.getInt(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG);
            }
            //构建核心组件：消息缓冲区
            this.accumulator = new RecordAccumulator(config.getInt(ProducerConfig.BATCH_SIZE_CONFIG),
                    this.totalMemorySize,
                    this.compressionType,
                    config.getLong(ProducerConfig.LINGER_MS_CONFIG),
                    retryBackoffMs,
                    metrics,
                    time);

            List<InetSocketAddress> addresses = ClientUtils.parseAndValidateAddresses(config.getList(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
            //更新元数据
            this.metadata.update(Cluster.bootstrap(addresses), time.milliseconds());
            ChannelBuilder channelBuilder = ClientUtils.createChannelBuilder(config.values());

            /**
             * CONNECTIONS_MAX_IDLE_MS_CONFIG： 一个网络连接最多空闲时间，默认9min
             * MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION：一个网络连接最大重试次数，默认为5次；由于kafka存在失败重试机制，若对信息数据顺序要求高，可将此参数设为1
             * SEND_BUFFER_CONFIG：socket发送数据缓冲区大小，默认128k
             * RECEIVE_BUFFER_CONFIG：socket接受数据缓冲区大小，默认32k
             */
            NetworkClient client = new NetworkClient(
                    new Selector(config.getLong(ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG), this.metrics, time, "producer", channelBuilder),
                    this.metadata,
                    clientId,
                    config.getInt(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION),
                    config.getLong(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG),
                    config.getInt(ProducerConfig.SEND_BUFFER_CONFIG),
                    config.getInt(ProducerConfig.RECEIVE_BUFFER_CONFIG),
                    this.requestTimeoutMs, time);

            /**
             * RETRIES：kafka重试机制次数
             * ACKS：
             *     0：producer发送至broker后即完成
             *     1：producer发送至broker后，根据主leader响应判断是成功
             *    -1：producer发送至broker后，根据主leader以及lsr中所有副本follower响应判断是成功
             */
            this.sender = new Sender(client,
                    this.metadata,
                    this.accumulator,
                    config.getInt(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION) == 1,
                    config.getInt(ProducerConfig.MAX_REQUEST_SIZE_CONFIG),
                    (short) parseAcks(config.getString(ProducerConfig.ACKS_CONFIG)),
                    config.getInt(ProducerConfig.RETRIES_CONFIG),
                    this.metrics,
                    new SystemTime(),
                    clientId,
                    this.requestTimeoutMs);
            String ioThreadName = "kafka-producer-network-thread" + (clientId.length() > 0 ? " | " + clientId : "");
            this.ioThread = new KafkaThread(ioThreadName, this.sender, true);
            this.ioThread.start();

            this.errors = this.metrics.sensor("errors");


            config.logUnused();
            AppInfoParser.registerAppInfo(JMX_PREFIX, clientId);
            log.debug("Kafka producer started");
        } catch (Throwable t) {
            // call close methods if internal objects are already constructed
            // this is to prevent resource leak. see KAFKA-2121
            close(0, TimeUnit.MILLISECONDS, true);
            // now propagate the exception
            throw new KafkaException("Failed to construct kafka producer", t);
        }
    }
