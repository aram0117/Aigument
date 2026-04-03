package com.example.aigument.domain.chatroom.controller;

import com.example.aigument.common.dto.response.CommonResponse;
import com.example.aigument.common.enums.UserRole;
import com.example.aigument.domain.auth.dto.AuthUser;
import com.example.aigument.domain.chatroom.dto.request.CreateChatRoomRequest;
import com.example.aigument.domain.chatroom.dto.response.CreateChatRoomResponse;
import com.example.aigument.domain.chatroom.dto.response.EnterChatRoomResponse;
import com.example.aigument.domain.chatroom.dto.response.GetChatRoomResponse;
import com.example.aigument.domain.chatroom.service.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatroom")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @Operation(summary = "채팅방 생성", description = "새로운 채팅방을 생성합니다.")
    @PostMapping
    public ResponseEntity<CommonResponse<CreateChatRoomResponse>> createChatRoom(@AuthenticationPrincipal AuthUser authUser, @RequestBody CreateChatRoomRequest request) {

        // 인증 정보가 없으면 임시로 1번 유저로 세팅 (테스트용)
        AuthUser testUser = (authUser != null) ? authUser : new AuthUser(1L, "홍길동", "test@test.com", UserRole.USER.toString());

        CreateChatRoomResponse response = chatRoomService.saveChatRoom(testUser, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success("채팅방 생성에 성공했습니다.", response));
    }

    @Operation(summary = "채팅방 단 건 조회", description = "해당 채팅방을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<GetChatRoomResponse>> getChatRoom(@PathVariable Long id) {

        GetChatRoomResponse response = chatRoomService.getChatRoom(id);

        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success("채팅방 단 건 조회에 성공했습니다.", response));
    }

    @Operation(summary = "게스트 채팅방 입장", description = "해당 채팅방으로 게스트가 입장합니다.")
    @PostMapping("/{id}/entry")
    public ResponseEntity<CommonResponse<EnterChatRoomResponse>> enterChatRoom(@PathVariable Long id, @AuthenticationPrincipal AuthUser authUser) {

        AuthUser testUser = (authUser != null) ? authUser : new AuthUser(2L, "심청", "test1@test.com", UserRole.USER.toString());

        EnterChatRoomResponse response = chatRoomService.enterChatRoom(id, testUser);

        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success("채팅방 입장에 성공했습니다.", response));
    }
}
