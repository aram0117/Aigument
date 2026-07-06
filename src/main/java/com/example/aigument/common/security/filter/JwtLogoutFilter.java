package com.example.aigument.common.security.filter;

import com.example.aigument.common.exception.CustomException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static com.example.aigument.common.exception.ErrorCode.LOGGED_OUT_TOKEN;

@Slf4j
@Component
public class JwtLogoutFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;
    private final HandlerExceptionResolver resolver;

    public JwtLogoutFilter(
            StringRedisTemplate redisTemplate,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.redisTemplate = redisTemplate;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) {

        try {
            String token = resolveToken(request);

            if (token != null) {
                String logoutStatus = redisTemplate.opsForValue().get(token);

                if ("logout".equals(logoutStatus)) {
                    throw new CustomException(LOGGED_OUT_TOKEN);
                }
            }
            // 다음 필터(BearerTokenAuthenticationFilter)로 반환
            filterChain.doFilter(request, response);

        } catch (CustomException e) {
            log.error("[JwtLogoutFilter] 로그아웃 처리된 토큰 재사용 시도 - URI: {}", request.getRequestURI());
            // 로그아웃 에러를 가로챔
            resolver.resolveException(request, response, null, e);
        } catch (Exception e) {
            log.error("[JwtLogoutFilter] 필터 처리 중 예상하지 못한 에러 발생 - URI: {}", request.getRequestURI(), e);
            // 모든 에러를 가로챔
            resolver.resolveException(request, response, null, e);
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}