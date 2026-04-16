package com.example.aigument.domain.chatroom.service;

import com.example.aigument.common.exception.CustomException;
import com.example.aigument.domain.auth.dto.AuthUser;
import com.example.aigument.domain.chatroom.dto.event.ChatRoomExitAndRemoveEvent;
import com.example.aigument.domain.chatroom.dto.event.GuestExitEvent;
import com.example.aigument.domain.chatroom.dto.request.CreateChatRoomRequest;
import com.example.aigument.domain.chatroom.dto.response.CreateChatRoomResponse;
import com.example.aigument.domain.chatroom.dto.response.EnterChatRoomResponse;
import com.example.aigument.domain.chatroom.dto.response.GetAllChatRoomResponse;
import com.example.aigument.domain.chatroom.dto.response.GetChatRoomResponse;
import com.example.aigument.domain.chatroom.entity.ChatRoom;
import com.example.aigument.domain.chatroom.repository.ChatRoomRepository;
import com.example.aigument.domain.chatroom.dto.event.ChatStartEvent;
import com.example.aigument.domain.user.entity.User;
import com.example.aigument.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.aigument.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CreateChatRoomResponse saveChatRoom(AuthUser authUser, CreateChatRoomRequest request) {

        // 인증 유저 조회
        User foundHost = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        validateSingleRoomPolicy(foundHost);

        // 채팅방 생성
        ChatRoom newChatRoom = new ChatRoom(request.getTitle(), request.getCategoryType(), request.getContent(), foundHost, null);

        // db 저장
        chatRoomRepository.save(newChatRoom);

        return CreateChatRoomResponse.from(newChatRoom);
    }


    @Transactional(readOnly = true)
    public GetChatRoomResponse getChatRoom(Long id) {

        // 채팅방 조회
        ChatRoom foundChatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() -> new CustomException(NOT_FOUND_CHATROOM));

        return GetChatRoomResponse.from(foundChatRoom);
    }


    @Transactional(readOnly = true)
    public List<GetAllChatRoomResponse> getAllChatRoom() {

        // 채팅방 목록 조회
        List<ChatRoom> foundAllChatRoom = chatRoomRepository.findAll();

        return foundAllChatRoom.stream()
                .map(GetAllChatRoomResponse::from)
                .toList();
    }


    @Transactional
    public EnterChatRoomResponse enterChatRoom(Long id, AuthUser authUser) {

        // 채팅방 조회
        ChatRoom foundChatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() -> new CustomException(NOT_FOUND_CHATROOM));

        nullCheckGuest(foundChatRoom.getGuest());

        // 참여할 게스트 조회
        User foundGuest = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        validateSingleRoomPolicy(foundGuest);

        // 새로운 게스트 채팅방 입장
        foundChatRoom.enterGuestChatRoom(foundGuest);

        // 채팅 시작 이벤트
        ChatStartEvent event = new ChatStartEvent(foundChatRoom.getId(), foundGuest.getId());

        // 채팅 시작 이벤트 발행
        eventPublisher.publishEvent(event);

        return EnterChatRoomResponse.from(foundChatRoom);
    }


    // 채팅방 나가기 처리
    @Transactional
    public void handleChatRoomExit(Long id, AuthUser authUser) {

        ChatRoom foundChatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() -> new CustomException(NOT_FOUND_CHATROOM));

        // 인증 유저가 게스트 일 때
        if (foundChatRoom.isGuest(authUser.getId())) {

            handleGuestExit(foundChatRoom);

            return;
        }

        // 유저(host)의 채팅방 존재 여부 검증
        validateChatRoomUser(foundChatRoom.getHost().getId(), authUser.getId());

        ChatRoomExitAndRemoveEvent event = new ChatRoomExitAndRemoveEvent(foundChatRoom.getId(), foundChatRoom.getHost().getId());

        // 채팅방 퇴장 후 삭제 이벤트 발행
        eventPublisher.publishEvent(event);

        // 채팅방 삭제
        chatRoomRepository.delete(foundChatRoom);
    }


    // 게스트 나가기 처리
    private void handleGuestExit(ChatRoom foundChatRoom) {

        GuestExitEvent event = new GuestExitEvent(foundChatRoom.getId(), foundChatRoom.getHost().getId(), foundChatRoom.getGuest().getId());

        // 게스트 퇴장 이벤트 발행
        eventPublisher.publishEvent(event);

        // 조회한 채팅방 게스트 삭제
        foundChatRoom.removeGuest();
    }



    /**
     * 검증 메서드
     */

    // 유저당 1인1실 정책
    private void validateSingleRoomPolicy(User user) {

        boolean hasActiveChatRoom = chatRoomRepository.existsByHost(user);

        // 이미 활성화된 채팅방이 존재할 경우
        if (hasActiveChatRoom) {
            throw new CustomException(ONLY_ONE_CHATROOM_ALLOWED);
        }
    }

    private void nullCheckGuest(User guest) {

        // 게스트가 존재하는 채팅방일 경우
        if (guest != null) {
            throw new CustomException(CHATROOM_ALREADY_FULL);
        }
    }

    private void validateChatRoomUser(Long hostId, Long authUserId) {

        // 해당 채팅방에 유저가 존재하지 않을 때
        if (!hostId.equals(authUserId)) {
            throw new CustomException(NOT_FOUND_USER_IN_CHATROOM);
        }
    }
}
