package com.example.aigument.common.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class RedisProperties {

    @Value("${redis.chat-log-ttl-hours:1}") // 채팅 로그 Redis 보관 시간
    private long chatLogTtlHours;
}
