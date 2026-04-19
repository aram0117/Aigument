package com.example.aigument.common.security.oauth2.handler;

import com.example.aigument.common.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.example.aigument.common.exception.ErrorCode.UNSUPPORTED_SOCIAL_PROVIDER;

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

            /*
                추후에 추가 예정
             */

        } else {
            throw new CustomException(UNSUPPORTED_SOCIAL_PROVIDER); // 지정하지 않는 소셜 로그인 예외 처리
        }
    }
}
