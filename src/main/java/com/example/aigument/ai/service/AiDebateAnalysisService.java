package com.example.aigument.ai.service;

import com.example.aigument.ai.model.ollama.OllamaAi;
import com.example.aigument.common.exception.CustomException;
import com.example.aigument.common.infra.redis.RedisKeys;
import com.example.aigument.domain.user.service.UserStatsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.aigument.common.exception.ErrorCode.AI_ANALYSIS_FAILED;
import static com.example.aigument.common.exception.ErrorCode.EMPTY_CHAT_LOG;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiDebateAnalysisService {

    private final RedissonClient redissonClient;
    private final StringRedisTemplate redisTemplate;
    private final OllamaAi ollamaAi;
    private final UserStatsService userStatsService;
    private final ObjectMapper objectMapper;
    private static final String ANALYZING = "AI 분석 중";


    @Transactional
    public void analyzeDebate(Long chatRoomId) {

        String logKey = RedisKeys.chatRoomLog(chatRoomId);
        String topicChannel = RedisKeys.chatRoomTopic(chatRoomId);

        redissonClient.getTopic(topicChannel).publish(ANALYZING);

        List<String> messages = redisTemplate.opsForList().range(logKey, 0, -1);

        if (messages == null || messages.isEmpty()) {
            throw new CustomException(EMPTY_CHAT_LOG);
        }

        String prompt = String.join("\n", messages);

        log.info("[AiAnalysisService] 토론 분석 시작 - ChatRoomId: {}", chatRoomId);

        ollamaAi.askOllama3(prompt)
                .subscribe(
                        result -> handleAiSuccess(result, topicChannel, logKey),
                        error -> handleAiError(error, topicChannel)
                );
    }


    private void handleAiSuccess(String result, String topicChannel, String logKey) {

        try {
            JsonNode jsonNode = objectMapper.readTree(result);

            String winner = jsonNode.get("winner").toString();
            String loser = jsonNode.get("loser").toString();
            String reason = jsonNode.get("reason").toString();

            userStatsService.incrementStatsCount(winner, loser);

            String msg = String.format(
                    "ai 분석이 완료되었습니다.\n\n[winner]%s\n\n[이유]\n%s",
                    winner, reason
            );

            redissonClient.getTopic(topicChannel).publish(msg);
            log.info("[AiAnalysisService] 분석 완료 퍼블리싱 성공 - Channel: {}", topicChannel);

            redisTemplate.delete(logKey);

        } catch (Exception e) {
            log.error("[AiAnalysisService] AI 응답 실패 (예상 응답 형식 - {\"winner\": \"유저ID\", \"loser\": \"유저ID\", \"reason\": \"승리 이유 요약\"}) \n 원본 응답: {}", result, e);
            handleAiError(new CustomException(AI_ANALYSIS_FAILED), topicChannel);
        }
    }

    private void handleAiError(Throwable error, String topicChannel) {

        log.error("[AiAnalysisService] 분석 프로세스 중 에러 발생 - Channel: {}", topicChannel, error);

        String errorMessage = error instanceof CustomException custom
                ? custom.getMessage()
                : "AI 토론 분석 중 알 수 없는 서버 오류가 발생했습니다.";

        String msg = String.format(
                "ai 분석 중 오류가 발생했습니다.\n\n[상세 사유]\n%s",
                errorMessage
        );

        redissonClient.getTopic(topicChannel).publish(msg);
    }
}
