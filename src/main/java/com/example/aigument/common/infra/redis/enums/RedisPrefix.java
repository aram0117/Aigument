package com.example.aigument.common.infra.redis.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisPrefix {

    CHATROOM_TOPIC_NAME("chat:room:topic:"),
    CHATROOM_LOG_NAME("chat:room:log:"),
    CHATROOM_PATTEN_NAME("chat:room:*"),

    SMS_AUTH_PREFIX("sms:auth:")
    ;

    private final String prefix;
}
