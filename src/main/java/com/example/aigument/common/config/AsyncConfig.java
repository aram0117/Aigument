package com.example.aigument.common.config; // 추천 경로

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.context.annotation.Bean;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync // 비동기 활성화 (필수!)
public class AsyncConfig {

    @Bean(name = "chatRoomAsyncExecutor")
    public Executor chatRoomAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);        // 기본 스레드 수
        executor.setMaxPoolSize(20);        // 최대 스레드 수
        executor.setQueueCapacity(500);     // 대기 큐 크기
        executor.setThreadNamePrefix("ChatRoomAsync-"); // 로그에서 확인할 스레드 이름 접두사
        executor.initialize();
        return executor;
    }

    @Bean(name = "chatRoomSessionAsyncExecutor")
    public Executor chatRoomSessionAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);        // 기본 스레드 수
        executor.setMaxPoolSize(20);        // 최대 스레드 수
        executor.setQueueCapacity(500);     // 대기 큐 크기
        executor.setThreadNamePrefix("chatRoomSessionAsync-"); // 로그에서 확인할 스레드 이름 접두사
        executor.initialize();
        return executor;
    }
}