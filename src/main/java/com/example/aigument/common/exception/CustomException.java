package com.example.aigument.common.exception;

import com.example.aigument.common.enums.ExceptionCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException{

    private final ExceptionCode exceptionCode;

    public CustomException(ExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }
}
