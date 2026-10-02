package com.example.aigument.domain.message.collector;

import com.example.aigument.ai.service.AiDebateAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.example.aigument.common.annotation.MeasureLatency;
import com.example.aigument.common.infra.redis.RedisKeys;
import com.example.aigument.common.properties.RedisProperties;

@Slf4j
@Component
@RequiredArgsConstructor
@MeasureLatency
public class ChatMessageCollector {

    private final StringRedisTemplate redisTemplate;
    private final RedisProperties redisProperties;
    private final AiDebateAnalysisService aiDebateAnalysisService;

    private final static Long chatSummaryThreshold = 10L;

    // 메시지 수집
    public void collect(Long chatRoomId, Long senderId, String message) {

        String logKey = RedisKeys.chatRoomLog(chatRoomId);

        String logEntry = String.format("[유저%s] %s", senderId, message);

        // 리스트 자료로 수집한 메시지 기록
        Long chatCount = redisTemplate.opsForList().rightPush(logKey, logEntry);

        // 채팅 요약 분기 (요약 실패가 메시지 수집 자체를 막지 않도록 별도로 방어)
        if (chatCount != null && chatCount.equals(chatSummaryThreshold)) {
            try {
                List<String> messages = redisTemplate.opsForList().range(logKey, 0, chatSummaryThreshold - 1);
                aiDebateAnalysisService.summarizeChatBranch(chatRoomId, messages);

                // 요약에 반영된 구간만 제거하고 새 분기 시작 (LTRIM은 양 끝 인덱스를 포함하므로 threshold부터 끝까지만 유지)
                redisTemplate.opsForList().trim(logKey, chatSummaryThreshold, -1);
            } catch (Exception e) {
                log.error("[ChatMessageCollector] 채팅 분기 요약 처리 실패 - ChatRoomId: {}, 원인: {}", chatRoomId, e.getMessage(), e);
            }
        }

        // 만료 시간 설정
        redisTemplate.expire(logKey, redisProperties.getChatLogTtlHours(), TimeUnit.HOURS);
    }
}
