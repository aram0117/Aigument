package com.example.aigument.common.security.oauth2.handler;

import com.example.aigument.common.security.oauth2.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import com.example.aigument.common.security.provider.JwtProvider;
import com.example.aigument.common.security.provider.RefreshTokenCookie;
import com.example.aigument.domain.auth.dto.SocialUser;
import com.example.aigument.domain.auth.service.SocialService;
import com.example.aigument.domain.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

import static com.example.aigument.common.enums.ExpirationTime.*;

@Component
@RequiredArgsConstructor
public abstract class AbstractSocialLoginJwtGrantSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${main.page.url}")
    private String mainPageUrl;

    protected final JwtProvider jwtProvider;
    protected final HttpCookieOAuth2AuthorizationRequestRepository cookieRepository;
    protected final SocialService socialService;
    protected final RefreshTokenCookie refreshTokenCookie;

    /**
     * 인증된 소셜 계정 정보 ex) google,kakao,naver등
     */
    protected abstract String extractProviderId(OAuth2User oAuth2User);
    protected abstract String extractEmail(OAuth2User oAuth2User);
    protected abstract String extractUserName(OAuth2User oAuth2User);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;

        // 소셜 로그인으로 가져온 인증 유저 정보
        String username = extractUserName(oAuth2User);
        String email = extractEmail(oAuth2User);
        String providerId = extractProviderId(oAuth2User);
        String provider = authToken.getAuthorizedClientRegistrationId().trim();

        SocialUser socialUser = new SocialUser(username, email, providerId, provider);

        User foundSocialUser = socialService.getSocialUser(socialUser);

        // 소셜 유저 정보로 jwt 토큰 발급
        String accessToken = jwtProvider.generateToken(foundSocialUser.getId(), foundSocialUser.getNickName(), foundSocialUser.getEmail(), foundSocialUser.getRole(), foundSocialUser.getProvider(), ACCESS_TOKEN_EXPIRATION_TIME.getExpirationTime());

        String refreshToken = jwtProvider.generateToken(foundSocialUser.getId(), foundSocialUser.getNickName(), foundSocialUser.getEmail(), foundSocialUser.getRole(), foundSocialUser.getProvider(), REFRESH_TOKEN_EXPIRATION_TIME.getExpirationTime());

        refreshTokenCookie.setRefreshTokenCookie(response, refreshToken.substring(7).trim());

        // 쿠키에 저장된 소셜 로그인 관련 요청 데이터 전부 삭제
        cookieRepository.removeAuthorizationRequest(request, response);

        // 파람에 JWT 토큰을 담아 로그인 페이지로 리다이렉트
        String targetUrl = UriComponentsBuilder.fromUriString(mainPageUrl)
                .queryParam("token", accessToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}