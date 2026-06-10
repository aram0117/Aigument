package com.example.aigument.domain.chatroom.broker;

import com.example.aigument.domain.chatroom.dto.event.ChatStartEvent;
import com.example.aigument.domain.chatroom.dto.event.SurrenderEvent;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
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

    /**
     * 채팅 시작 이벤트 발행
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void chatStartEventPublish(ChatStartEvent event) {

        String msg = String.format(
                """
                        [게스트%s]님이 입장하셨습니다.
                        토론을 시작해주세요.""",
                event.getGuestId()
        );

        globalEventPublisher(event.getChatRoomId(), msg);
    }


    /**
     * 항복 이벤트 발행
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void surrenderEventPublish(SurrenderEvent event) {

        String msg = String.format(
                """
                        [유저%s]님이 항복 하셨습니다.
                        """,
                event.getUserId()
        );

        globalEventPublisher(event.getChatRoomId(), msg);
    }


    /**
     *  공통 이벤트 발행
     */
    private void globalEventPublisher(Long chatRoomId, String msg) {

        String channel = RedisKeys.chatRoomTopic(chatRoomId);

        RTopic topic = redissonClient.getTopic(channel);

        topic.publish(msg);
    }
}
