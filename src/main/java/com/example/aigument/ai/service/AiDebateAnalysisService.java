package com.example.aigument.ai.service;

import com.example.aigument.ai.model.ollama.OllamaAi;
import com.example.aigument.ai.model.ollama.dto.OllamaJudgementResult;
import com.example.aigument.common.exception.CustomException;
import com.example.aigument.common.infra.redis.RedisKeys;
import com.example.aigument.domain.chatroom.entity.ChatRoom;
import com.example.aigument.domain.chatroom.repository.ChatRoomRepository;
import com.example.aigument.domain.user.service.UserStatsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.aigument.common.exception.ErrorCode.EMPTY_CHAT_LOG;
import static com.example.aigument.common.exception.ErrorCode.NOT_FOUND_CHATROOM;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiDebateAnalysisService {

    private final RedissonClient redissonClient;
    private final StringRedisTemplate redisTemplate;
    private final OllamaAi ollamaAi;
    private final UserStatsService userStatsService;
    private final ObjectMapper objectMapper;
    private final ChatRoomRepository chatRoomRepository;
    private final MeterRegistry meterRegistry;

    private static final String ANALYZING = "AI 분석 중";

    public void analyzeDebate(Long chatRoomId) {

        ChatRoom foundChatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_CHATROOM));

        Long foundChatRoomId = foundChatRoom.getId();
        String logKey = RedisKeys.chatRoomLog(foundChatRoomId);
        String topicChannel = RedisKeys.chatRoomTopic(foundChatRoomId);

        redissonClient.getTopic(topicChannel).publish(ANALYZING);

        List<String> messages = redisTemplate.opsForList().range(logKey, 0, -1);

        if (messages == null || messages.isEmpty()) {
            throw new CustomException(EMPTY_CHAT_LOG);
        }

        String prompt = String.join("\n", messages);

        log.info("[AiDebateAnalysisService] 토론 분석 시작 - ChatRoomId: {}", chatRoomId);

        Timer.Sample sample = Timer.start(meterRegistry);

        ollamaAi.askOllama3(prompt)
                .subscribe(
                        result -> {
                            handleAiSuccess(result, foundChatRoom, topicChannel, logKey);
                            recordAnalysisLatency(sample, "success");
                        },
                        error -> {
                            handleAiGlobalError(topicChannel);
                            recordAnalysisLatency(sample, "error");
                        }
                );
    }

    // Ollama 요청 전송부터 결과 처리 완료까지 전체 지연 시간 기록 (Grafana: aigument_ai_debate_analysis_latency_seconds)
    private void recordAnalysisLatency(Timer.Sample sample, String outcome) {
        sample.stop(Timer.builder("aigument.ai.debate.analysis.latency")
                .tag("outcome", outcome)
                .register(meterRegistry));
    }


    private void handleAiSuccess(String result, ChatRoom chatRoom, String topicChannel, String logKey) {

        try {
            OllamaJudgementResult judgement = objectMapper.readValue(result, OllamaJudgementResult.class);

            // 문자열중 유저id 값만 파싱
            Long winnerId = Long.parseLong(judgement.winner().replaceAll("[^0-9]", ""));
            Long loserId = Long.parseLong(judgement.loser().replaceAll("[^0-9]", ""));

            chatRoom.aiResultValidateUserInChatRoom(winnerId, loserId);

            userStatsService.incrementStatsCount(winnerId, loserId);

            String msg = String.format(
                    "ai 분석이 완료되었습니다.\n\n[winner]%s\n\n[이유]\n%s",
                    judgement.winner(), judgement.reason()
            );

            redissonClient.getTopic(topicChannel).publish(msg);
            log.info("[AiDebateAnalysisService] 분석 완료 퍼블리싱 성공 - Channel: {}", topicChannel);

            redisTemplate.delete(logKey);

        } catch (CustomException e) {
            log.error("[AiDebateAnalysisService] AI 응답 성공 처리중 예상치 못한 에러 (상세 사유 : {}", e.getMessage());
            handleAiGlobalError(topicChannel);
        } catch (Exception e) {
            log.error("[AiDebateAnalysisService] AI 규격 에러 (예상 응답 형식 - {\"winner\": \"유저ID\", \"loser\": \"유저ID\", \"reason\": \"승리 이유 요약\"}) \n 원본 응답: {}", result, e);
            handleAiGlobalError(topicChannel);
        }
    }

    private void handleAiGlobalError(String topicChannel) {

        redissonClient.getTopic(topicChannel).publish("ai 분석 중 알 수 없는 오류가 발생했습니다.\n 이용에 불편을 끼쳐드려 죄송합니다.");
    }
}
