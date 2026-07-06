package com.example.aigument.common.infra.websocket.handler;

import com.example.aigument.domain.auth.dto.AuthUser;
import com.example.aigument.domain.chatroom.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
@Async("chatRoomSessionAsyncExecutor")
public class WebSocketEventHandler {

    private final ChatRoomService chatRoomService;
    private final Map<String, Long> sessionRoomMap = new ConcurrentHashMap<>();

    private static final String SUB_DEST_PREFIX = "/sub/chat/room/topic/";


    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        String sessionId = accessor.getSessionId();

        // 유효하지 않은 경로면 즉시 종료
        if (!isValidDestination(destination)) {
            return;
        }

        try {
            Long chatRoomId = extractChatRoomId(destination);

            sessionRoomMap.put(sessionId, chatRoomId);

            log.info("[WebSocketEventHandler] 세션 입장 매핑 완료 - Session: {}, Room: {}", sessionId, chatRoomId);

        } catch (NumberFormatException e) {
            log.error("[WebSocketEventHandler] 채팅방 ID 파싱 실패: {}", destination, e);
        }
    }


    @EventListener
    public void handleSessionDisconnectEvent(SessionDisconnectEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        Long chatRoomId = sessionRoomMap.remove(sessionId);

        // 구독 정보가 없으면 즉시 종료 (채팅방에 입장하지 않은 정상적인 연결 종료)
        if (chatRoomId == null) {
            log.info("[WebSocketEventHandler] 매핑된 방 정보가 없는 세션 종료 - Session: {}", sessionId);
            return;
        }

        AuthUser authUser = extractAuthUser(accessor);

        // 구독된 세션인데 인증 정보가 없으면 비정상 상태로 간주
        if (authUser == null) {
            log.error("[WebSocketEventHandler] 구독된 세션에서 인증 정보를 찾을 수 없음 - Session: {}, Room: {}", sessionId, chatRoomId);
            return;
        }

        // 유저 나가기 처리
        log.info("[WebSocketEventHandler] 비정상 종료 감지: 유저 {} 가 방 {} 에서 퇴장", authUser.getId(), chatRoomId);
        chatRoomService.handleChatRoomExit(chatRoomId, authUser);
    }


    /**
     * 내부 메서드
     */

    private boolean isValidDestination(String destination) {

        return destination != null && destination.startsWith(SUB_DEST_PREFIX);
    }

    private Long extractChatRoomId(String destination) {

        String roomIdStr = destination.substring(destination.lastIndexOf("/") + 1);

        return Long.parseLong(roomIdStr);
    }

    private AuthUser extractAuthUser(StompHeaderAccessor accessor) {

        if (accessor.getUser() instanceof UsernamePasswordAuthenticationToken auth) {
            return (AuthUser) auth.getPrincipal();
        }

        return null;
    }
}