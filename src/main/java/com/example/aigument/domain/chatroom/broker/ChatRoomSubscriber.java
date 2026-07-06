package com.example.aigument.domain.chatroom.broker;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RPatternTopic;
import org.redisson.api.RedissonClient;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import com.example.aigument.common.infra.redis.RedisKeys;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRoomSubscriber {

    private final RedissonClient redissonClient;
    private final SimpMessageSendingOperations messagingTemplate;


    // 서버 실행 전 구독 초기화 세팅
    @PostConstruct
    public void init() {

        String patternTopic = RedisKeys.chatRoomPattern();
        RPatternTopic topic = redissonClient.getPatternTopic(patternTopic);


        /**
         * redis 채널에 수신자 등록
         * String 타입 발행
         */
        topic.addListener(String.class, (pattern, channel, msg) -> {

            String destination = convertToRoutingPath(channel.toString());

            messagingTemplate.convertAndSend(destination, msg); // 채팅 메시지 전송
        });

        log.info("[ChatRoomSubscriber] 채팅방 패턴 구독 초기화 완료 - Pattern: {}", patternTopic);
    }


    /**
     * 웹소켓 경로 컨버터 (redis name -> websocket path)
     */
    public String convertToRoutingPath(String channel) {
        return "/sub/" + channel.replace(":", "/");
    }
}
