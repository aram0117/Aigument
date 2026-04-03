package com.example.aigument.domain.chatroom.dto.request;

import com.example.aigument.common.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateChatRoomRequest {

    @NotBlank(message = "제목은 필수 입력 값입니다.")
    private String title;
    @NotBlank(message = "카테고리는 필수 입력 값입니다.")
    private CategoryType categoryType;
    private String content;
}
