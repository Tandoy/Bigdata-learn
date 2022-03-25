package com.flink.redis;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

public class TestRedis {

    public static void main(String[] args) {
        // get redis client
        // asle can use JRedis get the client
        RedisOptions redisOptions = new RedisOptions();
        redisOptions.setAddress("127.0.0.1");
        redisOptions.setPort(6379);

        VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setEventLoopPoolSize(10);
        vertxOptions.setWorkerPoolSize(20);

        Vertx vertx = Vertx.vertx(vertxOptions);
        RedisClient redisClient = RedisClient.create(vertx);

        String key = "a";
        redisClient.get(key,getRes -> {
            if (getRes.succeeded()) {
                String result = getRes.result();
                if (result == null) {
                    return;
                }else {
                    System.out.println(result);
                }
            }else return;
        });
    }
}
