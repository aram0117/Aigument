package com.example.aigument.common.infra.sms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class AuthCodeRequest {

    @NotBlank(message = "휴대폰 번호는 필수 입력 값입니다.")
    @Size(max = 13, message = "휴대폰번호는 -를 포함하여 최대 13자리까지 입력해주세요.")
    @Pattern(
            regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$",
            message = "올바른 휴대폰 번호 형식이 아닙니다. (예: 010-1234-5678)"
    )
    private String phoneNumber;
}
