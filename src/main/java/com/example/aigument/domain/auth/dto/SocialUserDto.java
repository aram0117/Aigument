package com.example.aigument.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SocialUserDto {

    private String username;
    private String email;
    private String providerId;
    private String provider;
}
