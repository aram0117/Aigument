package com.example.aigument.domain.user.dto.response;

import com.example.aigument.domain.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GetUserResponse {

    private final Long id;
    private final String nickName;
    private final String email;
    private final String password;
    private final String phoneNumber;

    public static GetUserResponse from(User user) {
        return new GetUserResponse(
                user.getId(),
                user.getNickName(),
                user.getEmail(),
                user.getPassword(),
                user.getPhoneNumber()
        );
    }
}
