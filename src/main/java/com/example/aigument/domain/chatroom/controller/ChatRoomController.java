package com.example.aigument.domain.chatroom.controller;

import com.example.aigument.common.dto.response.CommonResponse;
import com.example.aigument.domain.auth.dto.AuthUser;
import com.example.aigument.domain.chatroom.dto.request.CreateChatRoomRequest;
import com.example.aigument.domain.chatroom.dto.response.CreateChatRoomResponse;
import com.example.aigument.domain.chatroom.dto.response.EnterChatRoomResponse;
import com.example.aigument.domain.chatroom.dto.response.GetAllChatRoomResponse;
import com.example.aigument.domain.chatroom.dto.response.GetChatRoomResponse;
import com.example.aigument.domain.chatroom.service.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chatroom")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @Operation(summary = "채팅방 생성", description = "새로운 채팅방을 생성합니다.")
    @PostMapping
    public ResponseEntity<CommonResponse<CreateChatRoomResponse>> createChatRoom(@AuthenticationPrincipal AuthUser authUser, @RequestBody CreateChatRoomRequest request) {

        CreateChatRoomResponse response = chatRoomService.saveChatRoom(authUser, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success("채팅방 생성에 성공했습니다.", response));
    }

    @Operation(summary = "채팅방 상세 조회", description = "해당 채팅방을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<GetChatRoomResponse>> getChatRoom(@PathVariable Long id) {

        GetChatRoomResponse response = chatRoomService.getChatRoom(id);

        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success("채팅방 상세 조회에 성공했습니다.", response));
    }

    @Operation(summary = "채팅방 목록 조회", description = "채팅방 전체 목록을 조회합니다.")
    @GetMapping()
    public ResponseEntity<CommonResponse<List<GetAllChatRoomResponse>>> getAllChatRoom() {

        List<GetAllChatRoomResponse> response = chatRoomService.getAllChatRoom();

        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success("채팅방 목록 조회에 성공했습니다.", response));
    }
    @Operation(summary = "게스트 채팅방 입장", description = "해당 채팅방으로 게스트가 입장합니다.")
    @PostMapping("/{id}/entry")
    public ResponseEntity<CommonResponse<EnterChatRoomResponse>> enterChatRoom(@PathVariable Long id, @AuthenticationPrincipal AuthUser authUser) {

        EnterChatRoomResponse response = chatRoomService.enterChatRoom(id, authUser);

        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success("채팅방 입장에 성공했습니다.", response));
    }

    @Operation(summary = "채팅방 퇴장", description = "게스트가 퇴장하거나 호스트가 채팅방을 퇴장하여 삭제합니다.")
    @DeleteMapping("/{id}/exit")
    public ResponseEntity<CommonResponse<Void>> chatRoomExitAndRemove(@PathVariable Long id, @AuthenticationPrincipal AuthUser authUser) {

        chatRoomService.handleChatRoomExit(id, authUser);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(CommonResponse.success("채팅방을 퇴장합니다."));
    }
}
