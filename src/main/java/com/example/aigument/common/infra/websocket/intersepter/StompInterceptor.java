package com.example.aigument.common.infra.websocket.intersepter;

import com.example.aigument.common.exception.StompException;
import com.example.aigument.common.security.oauth2.converter.CustomAuthenticationConverter;
import com.example.aigument.domain.auth.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import static com.example.aigument.common.exception.ErrorCode.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompInterceptor implements ChannelInterceptor {

    private final CustomAuthenticationConverter authenticationConverter;
    private final JwtDecoder jwtDecoder; // 실제 토큰 서명과 만료를 검증할 디코더 필수 추가

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {

        // 스톰프 메시지 헤더 추출
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // 클라이언트로 부터 CONNECT 프레임을 받은 경우
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

            // Authorization 헤더 추출
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7).trim();

                try {
                    // 검증된 jwt 토큰
                    Jwt jwt = jwtDecoder.decode(token);

                    // 인증 객체 생성
                    AbstractAuthenticationToken authUser = authenticationConverter.convert(jwt);

                    // 스톰프 헤더에 인증 유저 저장
                    accessor.setUser(authUser);

                    accessor.setLeaveMutable(true);

                    log.info("[StompInterceptor] 웹소켓 연결 인증 성공 - UserId: {}", ((AuthUser) authUser.getPrincipal()).getId());

                    // 메시지 객체 반환
                    return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());

                } catch (JwtException e) {
                    log.error("[StompInterceptor] 웹소켓 연결 인증 실패 - 유효하지 않은 토큰: {}", e.getMessage());
                    throw new StompException(STOMP_INVALID_TOKEN);
                }
            } else {
                log.error("[StompInterceptor] 웹소켓 연결 인증 실패 - Authorization 헤더 누락");
                throw new StompException(STOMP_MISSING_AUTH);
            }
        }

        return message;
    }
}