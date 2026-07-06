package com.example.aigument.common.infra.websocket.handler;

import com.example.aigument.common.exception.StompException;
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
        if (cause instanceof StompException) {

            return handleStompException(cause);
        }

        // 그 외 에외 상황은 부모로 넘김
        return super.handleClientMessageProcessingError(clientMessage, e);
    }


    private Message<byte[]> handleStompException(Throwable e) {

        // 에러 프레임 생성
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);

        // message 헤더에 에러메시지 저장
        accessor.setMessage(e.getMessage());
        accessor.setLeaveMutable(true);

        byte[] payload = EMPTY_PAYLOAD;

        try {
            payload = objectMapper.writeValueAsBytes(accessor.getMessage());
        } catch (JsonProcessingException serializationException) {
            // 직렬화 실패 시 빈 payload로 대체 (에러 프레임 자체는 계속 전달)
        }

        return MessageBuilder.createMessage(payload, accessor.getMessageHeaders());
    }
}