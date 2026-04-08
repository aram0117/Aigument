package com.example.aigument.domain.chatroom.broker;

import com.example.aigument.domain.chatroom.dto.event.ChatStartEvent;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static com.example.aigument.common.infra.redis.enums.RedisPrefix.CHATROOM_TOPIC_NAME;

@Component
@RequiredArgsConstructor
public class ChatStartPublisher {

    private final RedissonClient redissonClient;

    /**
     * 채팅 시작 정보 발행
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void chatStartEventPublish(ChatStartEvent event) {

        String channel = CHATROOM_TOPIC_NAME.getPrefix() + event.getChatRoomId();

        RTopic topic = redissonClient.getTopic(channel);

        String msg = event.getGuestId() + "님이 입장하셨습니다. /n 토론을 진행해주세요.";

        // 시작 이벤트 메시지 발행
        topic.publish(msg);
    }
}
