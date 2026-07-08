package com.example.aigument.common.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class AsyncProperties {

    @Value("${async.chat-room.core-pool-size:10}")
    private int corePoolSize;

    @Value("${async.chat-room.max-pool-size:20}")
    private int maxPoolSize;

    @Value("${async.chat-room.queue-capacity:500}")
    private int queueCapacity;
}
