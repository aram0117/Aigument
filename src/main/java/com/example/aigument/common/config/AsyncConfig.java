package com.example.aigument.common.config;

import com.example.aigument.common.properties.AsyncProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.context.annotation.Bean;
import java.util.concurrent.Executor;

// @Async 어노테이션을 사용하려면 @EnableAsync가 반드시 활성화되어 있어야 한다
@Configuration
@EnableAsync
@RequiredArgsConstructor
public class AsyncConfig {

    private final AsyncProperties asyncProperties;

    @Bean(name = "chatRoomAsyncExecutor")
    public Executor chatRoomAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(asyncProperties.getCorePoolSize());
        executor.setMaxPoolSize(asyncProperties.getMaxPoolSize());
        executor.setQueueCapacity(asyncProperties.getQueueCapacity());
        executor.setThreadNamePrefix("ChatRoomAsync-"); // 로그에서 확인할 스레드 이름 접두사
        executor.initialize();
        return executor;
    }

    @Bean(name = "chatRoomSessionAsyncExecutor")
    public Executor chatRoomSessionAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(asyncProperties.getCorePoolSize());
        executor.setMaxPoolSize(asyncProperties.getMaxPoolSize());
        executor.setQueueCapacity(asyncProperties.getQueueCapacity());
        executor.setThreadNamePrefix("chatRoomSessionAsync-"); // 로그에서 확인할 스레드 이름 접두사
        executor.initialize();
        return executor;
    }
}