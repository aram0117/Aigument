package com.example.aigument.common.infra.websocket.config;

import com.example.aigument.common.infra.websocket.handler.StompErrorHandler;
import com.example.aigument.common.infra.websocket.intersepter.StompInterceptor;
import com.example.aigument.common.infra.websocket.resolver.StompPrincipalArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${server.url}")
    private String serverUrl;

    private final StompInterceptor stompInterceptor;
    private final StompErrorHandler stompErrorHandler;
    private final StompPrincipalArgumentResolver stompPrincipalArgumentResolver;

    public static long[] HEART_BEATS = {10000, 20000};


    /**
     * websocket 구독, 발행 경로 설정'
     * heartbeat 설정으로 좀비 세션 방지
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {

        config.enableSimpleBroker("/sub", "/user")
                .setTaskScheduler(heartbeatScheduler())
                .setHeartbeatValue(HEART_BEATS);
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * websocket 경로 설정
     * stomp 에러 처리 핸들러
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp").setAllowedOrigins(serverUrl).withSockJS();
        registry.setErrorHandler(stompErrorHandler);
    }

    /**
     * 토큰 인증 인터셉터
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompInterceptor);
    }

    /**
     * 인증 유저 아규먼트 리졸버
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(stompPrincipalArgumentResolver);
    }


    // 하트비트를 주기적으로 실행하기 위한 스레드 풀 설정
    @Bean
    public TaskScheduler heartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("wss-heartbeat-");
        return scheduler;
    }
}
