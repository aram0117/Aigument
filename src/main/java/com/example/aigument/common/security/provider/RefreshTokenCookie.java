package com.example.aigument.common.security.provider;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import static com.example.aigument.common.enums.ExpirationTime.REFRESH_TOKEN_EXPIRATION_TIME;

@Component
public class RefreshTokenCookie {

    /**
     * 쿠키에 저장할 리프레쉬 토큰
     * xss 방지
     * 모든 경로 허용
     * 리프레쉬 토큰 ttl 시간 == 쿠키 유효 ttl 시간
     * csrf 공격 방지 (Lax 설정)
     */
    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(REFRESH_TOKEN_EXPIRATION_TIME.getExpirationTime())
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .maxAge(0) // 삭제 명령
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}
