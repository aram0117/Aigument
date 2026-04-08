package com.example.aigument.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExpirationTime {

    ACCESS_TOKEN_EXPIRATION_TIME(60 * 30), // 30분
    REFRESH_TOKEN_EXPIRATION_TIME(60 * 60 * 24 * 14); // 2주

    private final long expirationTime;
}
