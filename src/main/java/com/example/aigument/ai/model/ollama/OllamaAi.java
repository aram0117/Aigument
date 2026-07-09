package com.example.aigument.ai.model.ollama;

import com.example.aigument.common.properties.AiProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
public class OllamaAi {

    private final WebClient webClient;
    private final MeterRegistry meterRegistry;
    private final String model;
    private final String keepAlive;

    public OllamaAi(AiProperties aiProperties, MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.model = aiProperties.getModelName();
        this.keepAlive = aiProperties.getModelKeepAlive();
        this.webClient = WebClient.builder()
                .baseUrl(aiProperties.getOllamaBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .responseTimeout(Duration.ofMinutes(aiProperties.getModelTtlMinutes()))
                ))
                .build();
    }

    public Mono<String> askOllama3(String prompt) {

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "prompt", prompt,
                "stream", false,
                "format", "json",
                "keep_alive", keepAlive
        );

        Timer.Sample sample = Timer.start(meterRegistry);

        return webClient.post()
                .uri("/api/generate")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(responseBody -> responseBody.get("response").toString())
                // 에러 발생 시 원본 원인을 파악하기 위한 로깅
                .doOnError(e -> log.error("[OllamaAi] AI 서버 요청 실패 - 원인: {}", e.getMessage()))
                // Ollama 응답이 실제로 도착(성공/실패)한 시점까지의 지연 시간을 기록 (Grafana: aigument_ollama_request_latency_seconds)
                .doFinally(signalType -> sample.stop(
                        Timer.builder("aigument.ollama.request.latency")
                                .tag("outcome", signalType.name())
                                .register(meterRegistry)));
    }
}
