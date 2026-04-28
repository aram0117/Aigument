package com.example.aigument.common.infra.sms.controller;

import com.example.aigument.common.dto.response.CommonResponse;
import com.example.aigument.common.infra.sms.dto.request.AuthCodeRequest;
import com.example.aigument.common.infra.sms.service.SolapiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SolapiService solapiService;

    @PostMapping("/send")
    public ResponseEntity<CommonResponse<Void>> sendAuthCode(@RequestBody @Valid AuthCodeRequest request) {

        solapiService.sendAuthCode(request);

        return ResponseEntity.status(HttpStatus.OK).body(CommonResponse.success("인증코드 전송을 성공했습니다."));
    }
}
