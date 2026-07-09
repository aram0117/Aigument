package com.example.aigument.domain.auth.service;

import com.example.aigument.common.annotation.MeasureLatency;
import org.springframework.stereotype.Service;

@Service
@MeasureLatency
public class LogoutRedirectService {

    /**
     * 공급자 브라우저 세션 종료 리다이렉트
     */
    public String getRedirectUrl(String provider) {

        if ("google".equals(provider)) {
            return "https://accounts.google.com/Logout";
        }

        // TODO: 신규 소셜 로그인 제공자 추가 시 이 아래에 분기를 추가한다

        // 로컬 계정 및 미지원 공급자는 기본 엔드포인트로 리다이렉트
        return "/";
    }
}
