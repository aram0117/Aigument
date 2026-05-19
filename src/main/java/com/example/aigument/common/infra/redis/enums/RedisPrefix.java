package com.example.aigument.common.infra.redis.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisPrefix {

    CHATROOM_TOPIC("chat:room:topic:"),
    CHATROOM_LOG("chat:room:log:"),
    CHATROOM_PATTERN("chat:room:*"),

    SMS_AUTH("sms:auth:")
    ;

    private final String prefix;
}
