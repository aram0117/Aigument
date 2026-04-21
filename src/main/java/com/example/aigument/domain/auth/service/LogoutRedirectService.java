package com.example.aigument.domain.auth.service;

import org.springframework.stereotype.Service;

@Service
public class LogoutRedirectService {

    /**
     * 공급자 브라우저 세션 종료 리다이렉트
     */
    public String getRedirectUrl(String provider) {

        if ("google".equals(provider)) {
            return "https://accounts.google.com/Logout";
        }

        /*
           추후에 추가 예정
         */

        // 로컬 로그아웃용 엔드 포인트
        return "/";
    }
}
