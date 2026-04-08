package com.example.aigument.domain.message.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ChatMessageRequest {

    @NotBlank(message = "전송자 아이디는 필수 입력 값입니다.")
    private Long senderId;
    @NotBlank(message = "메시지는 필수 입력 값입니다.")
    private String message;
}
