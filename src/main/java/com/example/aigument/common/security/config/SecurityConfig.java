package com.example.aigument.common.security.config;

import com.example.aigument.common.security.filter.JwtLogoutFilter;
import com.example.aigument.common.security.oauth2.converter.CustomAuthenticationConverter;
import com.example.aigument.common.security.oauth2.handler.GlobalSocialLonginSuccessHandler;
import com.example.aigument.common.security.oauth2.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import com.example.aigument.common.security.provider.JwtConfig;
import com.example.aigument.common.security.provider.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final ClientRegistrationRepository clientRegistrationRepository; // 인증 서버 접근 시 필요한 자원 저장소 (명시적 선언)
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository; // 쿠키 인증 요청 저장소 (default = 세션 인증)
    private final GlobalSocialLonginSuccessHandler globalSuccessHandler; // 모든 소셜 사용자 정보 처리
    private final CustomAuthenticationConverter authenticationConverter; // 인증 객체 반환 컨버터
    private final JwtLogoutFilter jwtLogoutFilter;
    private final JwtDecoder jwtDecoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/**").permitAll()
                        .requestMatchers("/ws-stomp/**").permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // 소셜 로그인
                .oauth2Login(oauth2 -> oauth2
                        // default login page : /oauth2/authorization/{provider}
                        .clientRegistrationRepository(clientRegistrationRepository)
                        .authorizationEndpoint(auth -> auth
                                .authorizationRequestRepository(cookieAuthorizationRequestRepository)
                        )
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/api/auth/login/oauth2/code/*")) // 공급자가 설정한 리다이렉트 주소
                        .successHandler(globalSuccessHandler) // 공급자로부터 받은 oauth 인증유저로 jwt 토큰 발급 후 처음으로 리다이렉트
                        .failureUrl("/api/auth/login/oauth2?error=true") // 실패 시 예외 처리
                )

                // oauth2ResourceServer에 내장된 BearerTokenAuthenticationFilter 실행 전 로그아웃 토큰 검증
                .addFilterBefore(jwtLogoutFilter, BearerTokenAuthenticationFilter.class)

                // oauth2ResourceServer 설정
                .oauth2ResourceServer((oauth2) -> oauth2
                        .jwt((jwt) -> jwt
                                .jwtAuthenticationConverter(authenticationConverter)
                                .decoder(jwtDecoder)
                        )
                );

        return http.build();
    }


    /**
     * cors 설정
     * 모든 Origin, Method, Header 쿠키 및 자격 증명 허용
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 스프링 시큐리티 기본 권한 접두사 "ROLE_" -> 빈 문자열 치환
     */
    @Bean
    public static GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults("");
    }
}


