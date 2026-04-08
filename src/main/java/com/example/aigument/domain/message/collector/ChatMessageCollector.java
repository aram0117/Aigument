package com.example.aigument.domain.message.collector;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.example.aigument.common.infra.redis.enums.RedisPrefix.CHATROOM_LOG_NAME;

@Component
@RequiredArgsConstructor
public class ChatMessageCollector {

    private final StringRedisTemplate redisTemplate;

    // 메시지 수집
    public void collect(Long chatRoomId, Long senderId, String message) {

        String key = CHATROOM_LOG_NAME.getPrefix() + chatRoomId;

        String log = String.format("[유저%s] %s", senderId, message);

        // 리스트 자료로 수집한 메시지 기록
        redisTemplate.opsForList().rightPush(key, log);

        // 만료 시간 설정
        redisTemplate.expire(key, 1, TimeUnit.HOURS);
    }
}
