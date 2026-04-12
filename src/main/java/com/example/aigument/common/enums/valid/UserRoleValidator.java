package com.example.aigument.common.enums.valid;

import com.example.aigument.common.enums.annotation.UserRoleValidAnnotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UserRoleValidator implements ConstraintValidator<UserRoleValidAnnotation, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {

        // 권한 null 체크
        return value != null;
    }
}