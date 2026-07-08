package com.example.aigument.common.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class WebSocketHeartbeatProperties {

    @Value("${websocket.heartbeat.client-interval-ms:10000}")
    private long clientIntervalMs;

    @Value("${websocket.heartbeat.server-interval-ms:20000}")
    private long serverIntervalMs;

    @Value("${websocket.heartbeat.scheduler-pool-size:1}")
    private int schedulerPoolSize;
}
