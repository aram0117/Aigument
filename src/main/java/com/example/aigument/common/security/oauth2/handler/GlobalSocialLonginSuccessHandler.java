package com.example.aigument.common.security.oauth2.handler;

import com.example.aigument.common.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.example.aigument.common.exception.ErrorCode.UNSUPPORTED_SOCIAL_PROVIDER;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalSocialLonginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final GoogleSocialLoginJwtGrantSuccessHandler googleSuccessHandler;

    /**
     *  모든 공급자의 OAuth 객체 공용 처리
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = authToken.getAuthorizedClientRegistrationId();

        if ("google".equals(registrationId)) {
            googleSuccessHandler.onAuthenticationSuccess(request, response, authentication);

            // TODO: 신규 소셜 로그인 제공자(카카오, 네이버 등) 추가 시 이 아래에 else if 분기를 추가한다

        } else {
            log.error("[GlobalSocialLonginSuccessHandler] 지원하지 않는 소셜 로그인 제공자 - registrationId: {}", registrationId);
            throw new CustomException(UNSUPPORTED_SOCIAL_PROVIDER); // 지정하지 않는 소셜 로그인 예외 처리
        }
    }
}
