package com.example.aigument.common.infra.redis.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisPrefix {

    CHATROOM_TOPIC("chat:room:topic:"),
    CHATROOM_LOG("chat:room:log:"),
    CHATROOM_PATTERN("chat:room:*"),
    CHATROOM_SUMMARY_LOG("chat:room:summary:"),
    SMS_AUTH("sms:auth:")
    ;

    private final String prefix;
}
