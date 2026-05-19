package com.example.aigument.domain.chatroom.broker;

import com.example.aigument.domain.chatroom.dto.event.ChatRoomExitAndRemoveEvent;
import com.example.aigument.domain.chatroom.dto.event.ChatStartEvent;
import com.example.aigument.domain.chatroom.dto.event.GuestExitEvent;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.aigument.common.infra.redis.RedisKeys;

@Async("chatRoomAsyncExecutor")
@Component
@RequiredArgsConstructor
public class ChatRoomPublisher {

    private final RedissonClient redissonClient;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String destination = "/chat/room";

    /**
     * 채팅 시작 이벤트 발행
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void chatStartEventPublish(ChatStartEvent event) {

        String channel = RedisKeys.chatRoomTopic(event.getChatRoomId());

        RTopic topic = redissonClient.getTopic(channel);

        String msg = String.format(
                """
                        [게스트%s]님이 입장하셨습니다.
                        토론을 시작해주세요.""",
                event.getGuestId()
        );

        // 시작 이벤트 메시지 발행
        topic.publish(msg);
    }


    /**
     * 게스트 퇴장 이벤트 발행
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void guestExitEventPublish(GuestExitEvent event) {

        String msg = String.format(
                """
                        [게스트%s]님이 퇴장하셨습니다.
                        다음 게스트 입장을 기다려주세요.""",
                event.getGuestId()
        );

        // 게스트 퇴장 이벤트 메시지를 호스트에게 발행
        messagingTemplate.convertAndSendToUser(String.valueOf(event.getHostId()), destination, msg);
    }


    /**
     * 채팅방 퇴장 후 삭제 이벤트 발행
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void chatRoomExitAndRemoveEventPublish(ChatRoomExitAndRemoveEvent event) {

        String msg = """
                채팅방을 퇴장하셨습니다.
                해당 채팅방은 자동 삭제됩니다.
                """;

        // 채팅방 퇴장 후 삭제 이벤트 메시지를 호스트에게 발행
        messagingTemplate.convertAndSendToUser(String.valueOf(event.getHostId()), destination, msg);
    }
}
