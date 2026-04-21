package com.example.aigument.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TokenResponse {

    private final String accessToken;

    @JsonIgnore
    private final String refreshToken; // JSON 응답 바디에서 제외 (쿠키로만 전달)

    public static TokenResponse from(String accessToken, String refreshToken) {
        return new TokenResponse(accessToken, refreshToken);
    }
}
