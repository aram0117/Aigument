package com.example.aigument.domain.chatroom.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomExitAndRemoveEvent {

    private Long ChatRoomId;
    private Long hostId;
}
