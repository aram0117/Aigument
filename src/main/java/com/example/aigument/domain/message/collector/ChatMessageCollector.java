package com.example.aigument.domain.message.collector;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import com.example.aigument.common.annotation.MeasureLatency;
import com.example.aigument.common.infra.redis.RedisKeys;
import com.example.aigument.common.properties.RedisProperties;

@Component
@RequiredArgsConstructor
@MeasureLatency
public class ChatMessageCollector {

    private final StringRedisTemplate redisTemplate;

    private final RedisProperties redisProperties;

    // 메시지 수집
    public void collect(Long chatRoomId, Long senderId, String message) {

        String logKey = RedisKeys.chatRoomLog(chatRoomId);

        String logEntry = String.format("[유저%s] %s", senderId, message);

        // 리스트 자료로 수집한 메시지 기록
        redisTemplate.opsForList().rightPush(logKey, logEntry);

        // 만료 시간 설정
        redisTemplate.expire(logKey, redisProperties.getChatLogTtlHours(), TimeUnit.HOURS);
    }
}
