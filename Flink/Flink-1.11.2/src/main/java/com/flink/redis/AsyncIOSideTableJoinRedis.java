package com.flink.redis;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.ResultTypeQueryable;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Flink通过异步IO实现redis维表join
 */
public class AsyncIOSideTableJoinRedis {

    private static final Logger logger = LoggerFactory.getLogger(AsyncIOSideTableJoinRedis.class);

    public static void main(String[] args) throws Exception {
        // set up the streaming execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // set ProcessingTime
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

        // source
        DataStreamSource<Row> source = env.addSource(new UserDefinedSource());

        // Transform Temporal Join
        // self async i/o
        SampleAsyncFunction sampleAsyncFunction = new SampleAsyncFunction();

        // add async operator to streaming job
        DataStream<String> result;

        if (true) {
            // 请求的顺序和返回的顺序保证一致
            result = AsyncDataStream.orderedWait(source
                    , sampleAsyncFunction
                    , 1000000L,
                    TimeUnit.MILLISECONDS, // 异步IO请求被视为失败的超时时间，超过该时间异步请求就算失败。该参数主要是为了剔除死掉或者失败的请求
                    20).setParallelism(1); // 同时最多有多少个异步请求在处理
        } else {
            // 请求元素的顺序与返回元素的顺序不保证一致
            result = AsyncDataStream.unorderedWait(
                    source,
                    sampleAsyncFunction,
                    10000,
                    TimeUnit.MILLISECONDS, // 异步IO请求被视为失败的超时时间，超过该时间异步请求就算失败。该参数主要是为了剔除死掉或者失败的请求
                    20).setParallelism(1); // 同时最多有多少个异步请求在处理
        }

        // sink
        result.print().setParallelism(1);

        // execute
        env.execute(AsyncIOSideTableJoinRedis.class.getCanonicalName());
    }

    private static class UserDefinedSource implements SourceFunction<Row>, ResultTypeQueryable<Row> {
        // 原子一致性保证
        private volatile boolean isCancel;

        @Override
        public TypeInformation<Row> getProducedType() {
            return new RowTypeInfo(TypeInformation.of(String.class), TypeInformation.of(String.class),
                    TypeInformation.of(Long.class));
        }

        @Override
        public void run(SourceContext<Row> sourceContext) throws Exception {
            if (!this.isCancel) {
                sourceContext.collect(Row.of("a", "b", 1L));
                Thread.sleep(1500000L);
            }
        }

        @Override
        public void cancel() {
            this.isCancel = true;
        }
    }

    private static class SampleAsyncFunction extends RichAsyncFunction<Row, String> {
        private transient RedisClient redisClient;

        @Override
        public void open(Configuration parameters) throws Exception {
            // get redis client
            // asle can use JRedis get the client
            RedisOptions redisOptions = new RedisOptions();
            redisOptions.setAddress("127.0.0.1");
            redisOptions.setPort(6379);

            VertxOptions vertxOptions = new VertxOptions();
            vertxOptions.setEventLoopPoolSize(10);
            vertxOptions.setWorkerPoolSize(20);

            Vertx vertx = Vertx.vertx(vertxOptions);
            redisClient = RedisClient.create(vertx);

        }

        @Override
        public void asyncInvoke(Row input, ResultFuture<String> resultFuture) {
            // get redis key value
            // redis key
            String key = input.getField(0).toString();
            redisClient.get(key, getRes -> {
                if (getRes.succeeded()) {
                    String result = getRes.result();
                    if (result == null) {
                        // redis have not the key value
                        resultFuture.complete(null);
                        return;
                    } else {
                        input.setField(2, result);
                        // redis value return
                        resultFuture.complete(Collections.singleton(input.toString()));
                    }
                } else if (getRes.failed()) {
                    resultFuture.complete(null);
                    return;
                }
            });
        }

        @Override
        public void close() throws Exception {
            if (redisClient != null) {
                redisClient.close(new Handler<AsyncResult<Void>>() {
                    @Override
                    public void handle(AsyncResult<Void> voidAsyncResult) {
                        logger.info("redis client is closed......");
                    }
                });
            }
        }
    }
}
