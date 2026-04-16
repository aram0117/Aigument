package com.example.aigument.domain.user.dto.response;

import com.example.aigument.domain.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UpdateUserResponse {

    private final Long id;
    private final String nickName;
    private final String email;
    private final String phoneNumber;

    public static UpdateUserResponse from(User user) {
        return new UpdateUserResponse(
                user.getId(),
                user.getNickName(),
                user.getEmail(),
                user.getPhoneNumber()
        );
    }
}
