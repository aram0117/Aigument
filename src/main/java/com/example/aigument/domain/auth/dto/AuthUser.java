package com.example.aigument.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthUser {

    /**
     * 시큐리티 컨택스트 홀더에 저장된 principal 객체
     */
    private Long id;
    private String username;
    private String email;
    private String role;
}
