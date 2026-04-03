package com.example.aigument.domain.auth.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SignupResponse {

    private final UserResponse createUserResponse;
    private final TokenResponse tokenResponse;

    public static SignupResponse from(UserResponse userResponse, TokenResponse tokenResponse) {

        return new SignupResponse(userResponse, tokenResponse);
    }
}
