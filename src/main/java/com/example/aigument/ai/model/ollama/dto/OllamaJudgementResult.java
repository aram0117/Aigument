package com.example.aigument.ai.model.ollama.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Ollama 판정 응답 규격 (winner/loser/reason)
 */
@JsonIgnoreProperties
public record OllamaJudgementResult(String winner, String loser, String reason) {
}
