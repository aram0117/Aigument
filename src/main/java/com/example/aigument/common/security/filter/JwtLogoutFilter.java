package com.example.aigument.common.security.filter;

import com.example.aigument.common.exception.CustomException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static com.example.aigument.common.exception.ErrorCode.LOGGED_OUT_TOKEN;

@Component
public class JwtLogoutFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, Object> redisTemplate;
    private final HandlerExceptionResolver resolver;

    public JwtLogoutFilter(
            RedisTemplate<String, Object> redisTemplate,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.redisTemplate = redisTemplate;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) {

        try {
            String token = resolveToken(request);

            if (token != null) {
                String logoutStatus = (String) redisTemplate.opsForValue().get(token);

                if ("logout".equals(logoutStatus)) {
                    throw new CustomException(LOGGED_OUT_TOKEN);
                }
            }
            // 다음 필터(BearerTokenAuthenticationFilter)로 반환
            filterChain.doFilter(request, response);

        } catch (CustomException e) {
            // 로그아웃 에러를 가로챔
            resolver.resolveException(request, response, null, e);
        } catch (Exception e) {
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