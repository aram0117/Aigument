package com.example.aigument.ai.service;

import com.example.aigument.ai.model.ollama.OllamaAi;
import com.example.aigument.common.exception.CustomException;
import com.example.aigument.domain.user.service.UserStatsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.aigument.common.exception.ErrorCode.AI_ANALYSIS_FAILED;
import static com.example.aigument.common.exception.ErrorCode.EMPTY_CHAT_LOG;
import static com.example.aigument.common.infra.redis.enums.RedisPrefix.CHATROOM_LOG_NAME;

@Service
@RequiredArgsConstructor
public class AiDebateAnalysisService {

    private final RedissonClient redissonClient;
    private final StringRedisTemplate redisTemplate;
    private final OllamaAi ollamaAi;
    private final UserStatsService userStatsService;
    private final ObjectMapper objectMapper;


    public void analyzeDebate(Long chatRoomId) {

        String key = CHATROOM_LOG_NAME.getPrefix() + chatRoomId;

        // 첫 채팅 부터 마지막 채팅 기록까지 가져오기
        List<String> messages = redisTemplate.opsForList().range(key, 0, -1);

        // 메시지 검증
        if (messages == null || messages.isEmpty()) {

            throw new CustomException(EMPTY_CHAT_LOG);
        }

        // 모든 채팅 기록을 하나의 문자열로 결합
        String fullChatLog = String.join("\n", messages);

        // 프롬프트 문자열 생성
        String prompt = createPrompt(fullChatLog);


        /**
         *  AI에 프롬프트 반환 (비동기 처리)
         *  치리후 ai 결과와 처리중 발생한 에러 메시지를 콜백
         */
        ollamaAi.askOllama3(prompt)
                .doOnSuccess(result -> sendAiSuccess(result, key))
                .onErrorMap(error -> new CustomException(AI_ANALYSIS_FAILED))
                .doOnError(customError -> sendAiError(customError.getMessage(), key))
                .subscribe();
    }


    /**
     * 프롬프트 생성
     */
    private String createPrompt(String fullChatLog) {

        return String.format(
                """
                        당신은 전문 토론 판정관입니다.
                        토론 로그를 확인하여 더 논리적이고 합리적인 의견을 제시한 유저를 승자로 선언하고 이유를 설명하세요.
                        응답은 반드시 지정한 결과 형식과 한국어로 답변 해주세요.
                        
                        ### 토론 로그 ###
                        %s
                        
                        ### 결과 형식 ###
                        {"winner": "유저ID", "loser": "유저ID", "reason": "승리 이유"}""",
                fullChatLog
        );
    }


    /**
     * AI 응답 성공과 실패를 클라이언트에게 알림
     */
    private void sendAiSuccess(String result, String channel) {

        try {

            RTopic topic = redissonClient.getTopic(channel);

            JsonNode jsonNode = objectMapper.readTree(result);

            String winner = jsonNode.get("winner").asText();
            String loser = jsonNode.get("loser").asText();
            String reason = jsonNode.get("reason").asText("승자의 근거가 더 타당 하다고 판단 하였습니다."); // ai 디폴트 응답

            // 승패 결과 반영
            userStatsService.incrementStatsCount(winner, loser);

            // ai 성공 응답
            String msg = String.format(
                    """
                            ai 분석이 완료되었습니다.
                            
                            [winner]
                            유저:%s
                            
                            [이유]
                            %s""",
                    winner, reason
            );

            topic.publish(msg);

        } catch (Exception e) {

            sendAiError(e.getMessage(), channel);
        }
    }

    private void sendAiError(String error, String channel) {

        RTopic topic = redissonClient.getTopic(channel);

        // ai 실패 응답
        String msg = String.format(
                """
                        ai 분석 중 오류가 발생했습니다.
                        
                        [상세 사유]
                        %s""",
                error
        );

        topic.publish(msg);
    }
}