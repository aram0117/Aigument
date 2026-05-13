package com.example.aigument.ai.service;

import com.example.aigument.ai.model.ollama.OllamaAi;
import com.example.aigument.common.exception.CustomException;
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
import static com.example.aigument.common.infra.redis.enums.RedisPrefix.CHATROOM_LOG_NAME;

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

        String channel = CHATROOM_LOG_NAME.getPrefix() + chatRoomId;

        // 프론트엔드에 '분석 중' 상태 알림
        redissonClient.getTopic(channel).publish(ANALYZING);

        List<String> messages = redisTemplate.opsForList().range(channel, 0, -1);

        if (messages == null || messages.isEmpty()) {
            throw new CustomException(EMPTY_CHAT_LOG);
        }

        String prompt = String.join("\n", messages);

        log.info("[AiAnalysisService] 토론 분석 시작 - ChatRoomId: {}", chatRoomId);

        // 핵심 리팩토링: WebFlux 체인 정리 및 에러 콜백 명시
        ollamaAi.askOllama3(prompt)
                .subscribe(
                        // onNext: 성공 시 처리 로직
                        result -> handleAiSuccess(result, channel),
                        // onError: 실패 시 처리 로직 (이 부분이 없어서 ErrorCallbackNotImplemented 발생)
                        error -> handleAiError(error, channel)
                );
    }


    private void handleAiSuccess(String result, String channel) {

        try {
            JsonNode jsonNode = objectMapper.readTree(result);

            // get() 대신 path()를 사용하여 NPE 방지 및 기본값 세팅
            String winner = jsonNode.path("winner").asText("승자 판독 불가");
            String loser = jsonNode.path("loser").asText("패자 판독 불가");
            String reason = jsonNode.path("reason").asText("승자의 근거가 더 타당하다고 판단하였습니다.");

            userStatsService.incrementStatsCount(winner, loser);

            String msg = String.format(
                    "ai 분석이 완료되었습니다.\n\n[winner]%s\n\n[이유]\n%s",
                    winner, reason
            );

            redissonClient.getTopic(channel).publish(msg);
            log.info("[AiAnalysisService] 분석 완료 퍼블리싱 성공 - Channel: {}", channel);

        } catch (Exception e) {
            log.error("[AiAnalysisService] AI 응답 실패. 원본 응답: {}", result, e);
            handleAiError(new CustomException(AI_ANALYSIS_FAILED), channel);
        }
    }

    private void handleAiError(Throwable error, String channel) {

        log.error("[AiAnalysisService] 분석 프로세스 중 에러 발생 - Channel: {}", channel, error);

        // 에러 타입에 따라 클라이언트에게 보여줄 메시지 결정
        String errorMessage = error instanceof CustomException
                ? error.getMessage()
                : "AI 토론 분석 중 알 수 없는 서버 오류가 발생했습니다.";

        String msg = String.format(
                "ai 분석 중 오류가 발생했습니다.\n\n[상세 사유]\n%s",
                errorMessage
        );

        // 실패 메시지 퍼블리싱 (이 과정에서 에러가 나면 더 이상 복구 불가하므로 try-catch 생략 또는 최소화)
        redissonClient.getTopic(channel).publish(msg);
    }
}