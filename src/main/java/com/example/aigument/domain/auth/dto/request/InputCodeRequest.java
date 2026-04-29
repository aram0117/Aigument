package com.example.aigument.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class InputCodeRequest {

    @NotBlank(message = "인증코드를 입력해주세요.")
    @Pattern(regexp = "^[0-9]{6}$", message = "6자리 숫자만 입력 가능합니다.")
    private String inputCode;
}
