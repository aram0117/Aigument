package com.example.aigument.common.infra.redis.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;


    /**
     * Lettuce 라이브러리 설정
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }


    /**
     * Redisson 라이브러리 설정
     */
    @Bean
    public RedissonClient redissonClient() {

        Config config = new Config();
        config.setCodec(new StringCodec());
        config.setNettyThreads(Runtime.getRuntime().availableProcessors() * 2);
        config.setLockWatchdogTimeout(1500);
        config.useSingleServer().setAddress("redis://" + host + ":" + port);

        return Redisson.create(config);
    }
}
