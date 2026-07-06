package com.example.aigument.common.infra.websocket.resolver;

import com.example.aigument.common.annotation.StompUser;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StompPrincipalArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 어노테이션이 체크
        return parameter.hasParameterAnnotation(StompUser.class);
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, @NonNull Message<?> message) {

        // 스톰프 메시지 헤더 추출
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || accessor.getUser() == null) {
            log.error("[StompPrincipalArgumentResolver] 스톰프 세션에 인증된 유저가 존재하지 않음");
            throw new RuntimeException("스톰프 세션에 인증된 유저가 존재하지 않습니다.");
        }

        // 스톰프 헤더 에서 유저 정보 추출
        Object user = accessor.getUser();

        // 인증 유저 타입 일때
        if (user instanceof Authentication authentication) {
            return authentication.getPrincipal(); // Principal(AuthUser) 인자 반환
        }

        return null;
    }
}
