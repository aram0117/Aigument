package com.example.aigument.common.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class JwtProperties {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.clock-skew-seconds:60}") // 서버 간 시간 오차 허용 범위
    private long clockSkewSeconds;

    @Value("${jwt.access-token-ttl-ms:1800000}") // 기본값 30분
    private long accessTokenExpirationTime;

    @Value("${jwt.refresh-token-ttl-ms:1209600000}") // 기본값 14일
    private long refreshTokenExpirationTime;
}
