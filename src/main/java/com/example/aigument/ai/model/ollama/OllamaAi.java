package com.example.aigument.ai.model.ollama;

import com.example.aigument.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static com.example.aigument.common.exception.ErrorCode.AI_COMMUNICATION_ERROR;
import static com.example.aigument.common.exception.ErrorCode.AI_RESPONSE_TIMEOUT;

@Slf4j
@Component
public class OllamaAi {

    private final WebClient webClient;

    public OllamaAi(@Value("${ollama.base.url}") String baseUrl) {

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .responseTimeout(Duration.ofSeconds(60)) // 최대 60초 대기
                ))
                .build();
    }

    public Mono<String> askOllama3(String prompt) {

        Map<String, Object> requestBody = Map.of(
                "model", "llama3",
                "prompt", prompt,
                "stream", false,
                "format", "json"
        );

        return webClient.post()
                .uri("/api/generate")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(res -> res.get("response").toString())
                // 에러 발생 시 원본 원인을 파악하기 위한 로깅
                .doOnError(e -> log.error("[OllamaAi] API Request Failed. Cause: {}", e.getMessage()))
                // 예외 타입에 따른 세밀한 매핑
                .onErrorMap(TimeoutException.class, e -> new CustomException(AI_RESPONSE_TIMEOUT))
                // CustomException이 아닌 기타 모든 에러는 통신 에러로 간주
                .onErrorMap(e -> !(e instanceof CustomException), e -> new CustomException(AI_COMMUNICATION_ERROR));
    }
}