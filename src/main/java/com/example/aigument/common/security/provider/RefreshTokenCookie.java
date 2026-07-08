package com.example.aigument.common.security.provider;

import com.example.aigument.common.properties.CookieProperties;
import com.example.aigument.common.properties.JwtProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenCookie {

    private final JwtProperties jwtProperties;
    private final CookieProperties cookieProperties;

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
                .secure(cookieProperties.isSecure())
                .path("/")
                .maxAge(jwtProperties.getRefreshTokenExpirationTime())
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .maxAge(0) // 삭제 명령
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}
