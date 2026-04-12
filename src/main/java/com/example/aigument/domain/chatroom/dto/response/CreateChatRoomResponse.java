package com.example.aigument.domain.chatroom.dto.response;

import com.example.aigument.common.enums.CategoryType;
import com.example.aigument.domain.chatroom.entity.ChatRoom;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateChatRoomResponse {

    private final Long id;
    private final String title;
    private final CategoryType category;
    private final String content;
    private final Long hostId;

    public static CreateChatRoomResponse from(ChatRoom chatRoom) {

        return new CreateChatRoomResponse(
                chatRoom.getId(),
                chatRoom.getTitle(),
                chatRoom.getCategory(),
                chatRoom.getContent(),
                chatRoom.getHost().getId()
        );
    }
}
