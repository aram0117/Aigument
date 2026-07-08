package com.example.aigument.common.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class CookieProperties {

    // 운영(HTTPS) 환경에서는 cookie.secure=true 로 재정의해야 한다. 로컬 http 개발 편의를 위해 기본값은 false.
    @Value("${cookie.secure:false}")
    private boolean secure;

    @Value("${oauth2.cookie.expire-seconds:180}") // 소셜 로그인 중간 쿠키 유효시간
    private int oauth2ExpireSeconds;
}
