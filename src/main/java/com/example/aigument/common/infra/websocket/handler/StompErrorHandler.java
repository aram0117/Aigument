package com.example.aigument.common.infra.websocket.handler;

import com.example.aigument.common.exception.StompAuthException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

@Getter
@Component
public class StompErrorHandler extends StompSubProtocolErrorHandler {

    private static final byte[] EMPTY_PAYLOAD = new byte[0];

    private final ObjectMapper objectMapper;

    StompErrorHandler(ObjectMapper objectMapper) {
        super();
        this.objectMapper = objectMapper;
    }

    @Override
    @Nullable
    public Message<byte[]> handleClientMessageProcessingError(@Nullable Message<byte[]> clientMessage, Throwable e) {

        Throwable cause = e.getCause();

        // 직접 설계한 예외 상황 발생시
        if (cause instanceof StompAuthException) {

            return handleAuthException(cause);
        }

        // 그 외 에외 상황은 부모로 넘김
        return super.handleClientMessageProcessingError(clientMessage, e);
    }


    private Message<byte[]> handleAuthException (Throwable e) {

        // 에러 프레임 생성
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);

        // message 헤더에 에러메시지 저장
        accessor.setMessage(e.getMessage());
        accessor.setLeaveMutable(true);

        byte[] payload = EMPTY_PAYLOAD;

        try {
            payload = objectMapper.writeValueAsBytes(accessor.getMessage());
        } catch (JsonProcessingException e2) {
            // 직렬화 에러 발생 시 payload 공백 처리
        }

        return MessageBuilder.createMessage(payload, accessor.getMessageHeaders());
    }
}