package com.example.aigument.domain.message.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class ChatMessageResponse {

    private final Long chatRoomId;
    private final Long senderId;
    private final String message;
    private final LocalDateTime createdAt;
}
