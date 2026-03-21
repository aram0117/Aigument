package com.example.aigument.common.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommonResponse<T>{

    private final boolean success;
    private final String message;
    private final T data;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;

    public CommonResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // 성공 응답 (데이터 없음)
    public static <T> CommonResponse<T> success(String message) {
        return new CommonResponse<>(true, message, null);
    }

    // 성공 응답 (커스텀 메시지)
    public static <T> CommonResponse<T> success(String message, T data) {
        return new CommonResponse<>(true, message, data);
    }

    // 실패 응답
    public static <T> CommonResponse<T> error(String message) {
        return new CommonResponse<>(false, message, null);
    }

    // 실패 응답 (데이터 포함)
    public static <T> CommonResponse<T> error(String message, T data) {
        return new CommonResponse<>(false, message, data);
    }
}
