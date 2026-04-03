package com.example.aigument.common.valid;

import com.example.aigument.common.annotation.EnumValidAnnotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EnumValidator implements ConstraintValidator<EnumValidAnnotation, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {

        // 권한 null 체크
        return value != null;
    }
}