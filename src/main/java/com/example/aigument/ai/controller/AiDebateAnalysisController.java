package com.example.aigument.ai.controller;

import com.example.aigument.ai.service.AiDebateAnalysisService;
import com.example.aigument.common.dto.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiDebateAnalysisController {

    private final AiDebateAnalysisService aiDebateAnalysisService;

    @Operation(summary = "채팅방 토론 기록 ai 분석", description = "채팅방 모든 메시지 기록을 ai가 분석합니다.")
    @PostMapping("/chatRoom/{chatRoomId}/analysis")
    public ResponseEntity<CommonResponse<Void>> aiAnalyzeDebateLog(@PathVariable Long chatRoomId) {

        aiDebateAnalysisService.analyzeDebate(chatRoomId);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(CommonResponse.success("AI가 토론 내용을 분석 중입니다. 판정 결과가 나올 때까지 잠시만 기다려 주세요."));
    }
}
