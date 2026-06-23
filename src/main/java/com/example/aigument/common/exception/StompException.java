package com.example.aigument.common.exception;

import lombok.Getter;

@Getter
public class StompException extends RuntimeException {

    private final ErrorCode errorCode;

    public StompException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}