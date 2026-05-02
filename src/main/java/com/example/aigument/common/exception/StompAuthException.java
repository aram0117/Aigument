package com.example.aigument.common.exception;

import lombok.Getter;

@Getter
public class StompAuthException extends RuntimeException {

    private final ErrorCode errorCode;

    public StompAuthException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}