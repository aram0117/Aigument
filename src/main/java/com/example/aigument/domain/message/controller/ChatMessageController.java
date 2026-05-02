package com.example.aigument.domain.message.controller;

import com.example.aigument.common.annotation.StompUser;
import com.example.aigument.domain.auth.dto.AuthUser;
import com.example.aigument.domain.message.collector.ChatMessageCollector;
import com.example.aigument.domain.message.dto.request.ChatMessageRequest;
import com.example.aigument.domain.message.dto.response.ChatMessageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.time.Clock;
import java.time.LocalDateTime;

import static com.example.aigument.common.infra.redis.enums.RedisPrefix.CHATROOM_TOPIC_NAME;

@Tag(name = "chat_message", description = "채팅 메시지 api")
@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageCollector chatMessageCollector;
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    @Operation(summary = "채팅 메시지 전송", description = "두명의 유저가 채팅 메시지를 주고받습니다.")
    @MessageMapping("{chatRoomId}")
    public void sendMessage(@DestinationVariable Long chatRoomId, @Valid ChatMessageRequest request, @StompUser AuthUser authUser) throws Exception {

        // 메시지 응답 객체 생성
        ChatMessageResponse response = ChatMessageResponse.builder()
                .chatRoomId(chatRoomId)
                .senderId(authUser.getId())
                .message(request.getMessage())
                .createdAt(LocalDateTime.now(Clock.systemUTC()))
                .build();

        // 전송한 메시지를 수집
        chatMessageCollector.collect(response.getChatRoomId(), response.getSenderId(), response.getMessage());

        String channel = CHATROOM_TOPIC_NAME.getPrefix() + chatRoomId;

        RTopic topic = redissonClient.getTopic(channel);

        // 제이슨 문자열
        String jsonMsg = objectMapper.writeValueAsString(response);

        // 채팅 메시지 발행
        topic.publish(jsonMsg);
    }
}
