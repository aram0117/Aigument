package com.example.aigument.domain.chatroom.dto.response;

import com.example.aigument.common.enums.CategoryType;
import com.example.aigument.domain.chatroom.entity.ChatRoom;
import com.example.aigument.domain.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Getter
@RequiredArgsConstructor
public class GetAllChatRoomResponse {

    private final Long id;
    private final String title;
    private final CategoryType category;
    private final String content;
    private final Long hostId;
    private final Long guestId;

    public static GetAllChatRoomResponse from(ChatRoom chatRoom) {

        return new GetAllChatRoomResponse(
                chatRoom.getId(),
                chatRoom.getTitle(),
                chatRoom.getCategory(),
                chatRoom.getContent(),
                chatRoom.getHost().getId(),
                Optional.ofNullable(chatRoom.getGuest())
                        .map(User::getId)
                        .orElse(0L) // 게스트가 null 일 때 0응답 -> 프론트에서 "대기중" 으로 변경
        );
    }
}
