package com.example.aigument.domain.chatroom.service;

import com.example.aigument.common.exception.CustomException;
import com.example.aigument.domain.auth.dto.AuthUser;
import com.example.aigument.domain.chatroom.dto.event.SurrenderEvent;
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
import com.example.aigument.domain.user.service.UserStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.aigument.common.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserStatsService userStatsService;

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

        log.info("[ChatRoomService] 채팅방 생성 - ChatRoomId: {}, HostId: {}", newChatRoom.getId(), foundHost.getId());

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

        log.info("[ChatRoomService] 채팅방 입장 - ChatRoomId: {}, GuestId: {}", foundChatRoom.getId(), foundGuest.getId());

        return EnterChatRoomResponse.from(foundChatRoom);
    }


    // 채팅방 나가기 처리 (항복 선언)
    @Transactional
    public void handleChatRoomExit(Long id, AuthUser authUser) {

        ChatRoom foundChatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() -> new CustomException(NOT_FOUND_CHATROOM));

        // 채팅방 게스트 존재 여부 (클라이언트 == 게스트)
        if (foundChatRoom.isGuest(authUser.getId())) {

            isSurrender(foundChatRoom, authUser.getId());

            return;
        }

        // 유저(host)의 채팅방 존재 여부 검증
        validateChatRoomUser(foundChatRoom.getHost().getId(), authUser.getId());

        // 게스트가 존재하지 않으면 단순 삭제 처리
        if (foundChatRoom.getGuest() == null) {

            chatRoomRepository.delete(foundChatRoom);

            log.info("[ChatRoomService] 채팅방 삭제 - ChatRoomId: {}, HostId: {} (게스트 없이 호스트 퇴장)", id, authUser.getId());

            return;  // isSurrender 호출하지 않음

        }

        isSurrender(foundChatRoom, authUser.getId());
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

    // 항복 선언 -> 패배 처리 -> 삭제
    private void isSurrender(ChatRoom chatRoom, Long userId) {

        SurrenderEvent event = new SurrenderEvent(chatRoom.getId(), userId);

        eventPublisher.publishEvent(event);

        userStatsService.getOrCreateUserStats(userId)
                .incrementLossCount();

        chatRoomRepository.delete(chatRoom);

        log.info("[ChatRoomService] 항복 처리 완료 - ChatRoomId: {}, UserId: {}", chatRoom.getId(), userId);
    }
}
