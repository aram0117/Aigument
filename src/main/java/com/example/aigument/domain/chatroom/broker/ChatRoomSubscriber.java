package com.example.aigument.domain.chatroom.broker;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RPatternTopic;
import org.redisson.api.RedissonClient;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import static com.example.aigument.common.infra.redis.enums.RedisPrefix.CHATROOM_PATTEN_NAME;

@Component
@RequiredArgsConstructor
public class ChatRoomSubscriber {

    private final RedissonClient redissonClient;
    private final SimpMessageSendingOperations messagingTemplate;


    // 서버 실행 전 구독 초기화 세팅
    @PostConstruct
    public void init() {

        String patternTopic = CHATROOM_PATTEN_NAME.getPrefix();
        RPatternTopic topic = redissonClient.getPatternTopic(patternTopic);


        /**
         * redis 채널에 수신자 등록
         * String 타입 발행
         */
        topic.addListener(String.class, (pattern, channel, msg) -> {

            String destination = convertToRoutingPath(channel.toString());

            messagingTemplate.convertAndSend(destination, msg); // 채팅 메시지 전송
        });
    }


    /**
     * 웹소켓 경로 컨버터 (redis name -> websocket path)
     */
    public String convertToRoutingPath(String channel) {
        return "/sub/" + channel.replace(":", "/");
    }
}
