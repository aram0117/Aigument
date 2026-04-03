package com.example.aigument.domain.auth.dto.response;

import com.example.aigument.domain.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserResponse {

    private final Long id;
    private final String nickName;
    private final String email;
    private final String phoneNumber;

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getNickName(),
                user.getEmail(),
                user.getPhoneNumber()
        );
    }
}
