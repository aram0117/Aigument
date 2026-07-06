package com.example.aigument.common.security.oauth2.repository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    // 환경 변수에서 쿠키 보안 설정을 가져옴
    @Value("${cookie.secure}")
    private boolean cookieSecure;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
                .map(this::deserialize)
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            this.removeAuthorizationRequestCookies(request, response);
            return;
        }

        // 1. OAuth2 인증 요청 정보 저장
        String serializedRequest = serialize(authorizationRequest);
        addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, serializedRequest);

        // 2. 사후 리다이렉트를 위한 URI 저장 (프론트엔드에서 파라미터로 넘긴 경우)
        String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        if (StringUtils.hasText(redirectUriAfterLogin)) {
            addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        OAuth2AuthorizationRequest authRequest = this.loadAuthorizationRequest(request);
        if (authRequest != null) {
            this.removeAuthorizationRequestCookies(request, response);
        }
        return authRequest;
    }

    /**
     * 인증 과정에서 사용된 모든 쿠키를 삭제합니다.
     */
    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
    }

    // --- 유틸리티 메서드 ---

    private void addCookie(HttpServletResponse response, String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(COOKIE_EXPIRE_SECONDS);
        cookie.setSecure(cookieSecure); // 환경 변수에 따라 HTTPS 요구 여부 결정

        // 최신 브라우저를 위한 SameSite 설정
        cookie.setAttribute("SameSite", "Lax");

        response.addCookie(cookie);
    }

    private void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }
    }

    private Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals(name))
                    .findFirst();
        }
        return Optional.empty();
    }

    // --- 직렬화 로직 ---

    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(authorizationRequest);
            return Base64.getUrlEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            throw new IllegalArgumentException("OAuth2AuthorizationRequest 직렬화 실패", e);
        }
    }

    private OAuth2AuthorizationRequest deserialize(Cookie cookie) {
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(cookie.getValue());
            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decodedBytes);
                 ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
                return (OAuth2AuthorizationRequest) objectInputStream.readObject();
            }
        } catch (Exception e) {
            return null; // 변조되거나 오류 발생 시 무시
        }
    }
}