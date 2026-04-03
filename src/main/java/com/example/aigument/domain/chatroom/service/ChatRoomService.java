package com.example.aigument.domain.chatroom.service;

import com.example.aigument.common.exception.CustomException;
import com.example.aigument.domain.auth.dto.AuthUser;
import com.example.aigument.domain.chatroom.dto.request.CreateChatRoomRequest;
import com.example.aigument.domain.chatroom.dto.response.CreateChatRoomResponse;
import com.example.aigument.domain.chatroom.dto.response.EnterChatRoomResponse;
import com.example.aigument.domain.chatroom.dto.response.GetChatRoomResponse;
import com.example.aigument.domain.chatroom.entity.ChatRoom;
import com.example.aigument.domain.chatroom.repository.ChatRoomRepository;
import com.example.aigument.domain.chatroom.dto.event.ChatStartEvent;
import com.example.aigument.domain.user.entity.User;
import com.example.aigument.domain.user.repoistory.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
