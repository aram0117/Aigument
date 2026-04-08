package com.example.aigument.ai.model.ollama;

import com.example.aigument.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Map;

import static com.example.aigument.common.exception.ErrorCode.AI_COMMUNICATION_ERROR;
import static com.example.aigument.common.exception.ErrorCode.AI_RESPONSE_TIMEOUT;

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

    /**
     * Ollama3 공식 문서 기준 RestAPI 규격
     * 출처 = <a href="https://docs.ollama.com/api/generate">...</a>
     */
    public Mono<String> askOllama3(String prompt) {

        Map<String, Object> requestBody = Map.of(
                "model", "llama3",
                "prompt", prompt,
                "stream", false,  // 비스트링 방식
                "format", "json" // ai 응답은 json 형태로 받음
        );

        return webClient.post()
                .uri("/api/generate")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(res -> res.get("response").toString())
                .onErrorMap(e -> {

                    // ai 타임 아웃으로 인한 에러 응답
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        return new CustomException(AI_RESPONSE_TIMEOUT);
                    }
                    // ai 서버 관련 에러 응답
                    return new CustomException(AI_COMMUNICATION_ERROR);
                });
    }
}
