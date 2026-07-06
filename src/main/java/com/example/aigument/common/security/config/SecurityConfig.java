package com.example.aigument.common.security.config;

import com.example.aigument.common.security.filter.JwtLogoutFilter;
import com.example.aigument.common.security.oauth2.converter.CustomAuthenticationConverter;
import com.example.aigument.common.security.oauth2.handler.GlobalSocialLonginSuccessHandler;
import com.example.aigument.common.security.oauth2.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ClientRegistrationRepository clientRegistrationRepository; // 인증 서버 접근 시 필요한 자원 저장소 (명시적 선언)
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository; // 쿠키 인증 요청 저장소 (default = 세션 인증)
    private final GlobalSocialLonginSuccessHandler globalSuccessHandler; // 모든 소셜 사용자 정보 처리
    private final CustomAuthenticationConverter authenticationConverter; // 인증 객체 반환 컨버터
    private final JwtLogoutFilter jwtLogoutFilter;
    private final JwtDecoder jwtDecoder;

    // 프론트엔드 허용 출처 (콤마로 다중 등록 가능, 로컬 개발 기본값 포함)
    @Value("${cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

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
                        // 인증 없이 허용: 회원가입·로그인·SMS 인증·OAuth2
                        .requestMatchers(
                                "/api/auth/signup",
                                "/api/auth/login",
                                "/api/auth/login/oauth2/**",
                                "/api/sms/**",
                                "/oauth2/**"
                        ).permitAll()
                        // STOMP: CONNECT 시 StompInterceptor에서 JWT 검증
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
     * 자격 증명(쿠키)을 허용하므로 Origin은 와일드카드가 아닌 허용 목록(cors.allowed-origins)만 등록한다.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .forEach(configuration::addAllowedOriginPattern);
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


