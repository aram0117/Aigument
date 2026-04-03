package com.example.aigument.common.security.oauth2.handler;

import com.example.aigument.common.security.oauth2.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import com.example.aigument.common.security.provider.JwtProvider;
import com.example.aigument.common.security.provider.RefreshTokenCookie;
import com.example.aigument.domain.auth.service.SocialService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class GoogleSocialLoginJwtGrantSuccessHandler extends AbstractSocialLoginJwtGrantSuccessHandler{

    public GoogleSocialLoginJwtGrantSuccessHandler(JwtProvider jwtProvider, HttpCookieOAuth2AuthorizationRequestRepository cookieRepository, SocialService socialService, RefreshTokenCookie refreshTokenCookie) {
        super(jwtProvider, cookieRepository, socialService, refreshTokenCookie);
    }

    /**
     * 구글의 고유 식별값인 'sub' (Subject)를 추출합니다.
     * 이 값은 이메일이 바뀌어도 변하지 않는 불변의 값입니다.
     */
    @Override
    protected String extractProviderId(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute("sub");
    }

    /**
     * 구글 계정의 이메일 정보를 추출합니다.
     */
    @Override
    protected String extractEmail(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute("email");
    }

    /**
     * 구글 프로필에 등록된 사용자 이름을 추출합니다.
     */
    @Override
    protected String extractUserName(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute("name");
    }
}
