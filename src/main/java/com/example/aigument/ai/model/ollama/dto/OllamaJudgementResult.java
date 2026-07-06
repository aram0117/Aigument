package com.example.aigument.ai.model.ollama.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Ollama 판정 응답 스키마 (winner/loser/reason)
 * Jackson이 JSON 문자열을 직접 필드에 바인딩하므로 따옴표가 섞이는 문제(JsonNode#toString())가 발생하지 않는다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OllamaJudgementResult(String winner, String loser, String reason) {
}
