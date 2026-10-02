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
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
public class OllamaAi {

    private final WebClient webClient;
    private final MeterRegistry meterRegistry;
    private final String model;
    private final String keepAlive;
    private final long retryMaxAttempts;
    private final Duration retryBackoffInitial;
    private final Duration retryBackoffMax;

    public OllamaAi(AiProperties aiProperties, MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.model = aiProperties.getModelName();
        this.keepAlive = aiProperties.getModelKeepAlive();
        this.retryMaxAttempts = aiProperties.getRetryMaxAttempts();
        this.retryBackoffInitial = Duration.ofSeconds(aiProperties.getRetryBackoffInitialSeconds());
        this.retryBackoffMax = Duration.ofSeconds(aiProperties.getRetryBackoffMaxSeconds());
        this.webClient = WebClient.builder()
                .baseUrl(aiProperties.getOllamaBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .responseTimeout(Duration.ofMinutes(aiProperties.getModelTtlMinutes()))
                ))
                .build();
    }

    // operation: 호출 지점 구분용 태그 (예: "analyze", "summarize") - 로그/메트릭에서 어떤 흐름의 요청인지 구분하기 위함
    public Mono<String> askOllama3(String prompt, String operation) {

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
                // 일시적 장애(연결 실패, 타임아웃, 5xx 등)에 한해 지수 백오프로 재시도. 4xx(요청 자체 문제)는 재시도해도 결과가 같으므로 제외
                .retryWhen(Retry.backoff(retryMaxAttempts, retryBackoffInitial)
                        .maxBackoff(retryBackoffMax)
                        .filter(OllamaAi::isRetryable)
                        .doBeforeRetry(signal -> {
                            log.warn("[OllamaAi] AI 서버 요청 재시도 ({}) {}/{} - 원인: {}",
                                    operation, signal.totalRetries() + 1, retryMaxAttempts, signal.failure().getMessage());
                            meterRegistry.counter("aigument.ollama.request.retry", "operation", operation).increment();
                        })
                        // 재시도가 모두 소진되면 RetryExhaustedException 대신 마지막 원인 예외를 그대로 전파
                        .onRetryExhaustedThrow((retrySpec, signal) -> signal.failure()))
                // 에러 발생 시(재시도 모두 소진 포함) 원본 원인을 파악하기 위한 로깅 및 실패 횟수 기록 (Grafana: aigument_ollama_request_failure_total)
                .doOnError(e -> {
                    log.error("[OllamaAi] AI 서버 요청 최종 실패 ({}) - 원인: {}", operation, e.getMessage());
                    meterRegistry.counter("aigument.ollama.request.failure", "operation", operation).increment();
                })
                // Ollama 응답이 실제로 도착(성공/실패)한 시점까지의 지연 시간을 기록 (Grafana: aigument_ollama_request_latency_seconds)
                .doFinally(signalType -> sample.stop(
                        Timer.builder("aigument.ollama.request.latency")
                                .tag("operation", operation)
                                .tag("outcome", signalType.name())
                                .register(meterRegistry)));
    }

    private static boolean isRetryable(Throwable throwable) {
        if (throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException responseException) {
            return responseException.getStatusCode().is5xxServerError();
        }
        return true;
    }
}
