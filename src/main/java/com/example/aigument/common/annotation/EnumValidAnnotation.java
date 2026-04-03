package com.example.aigument.common.annotation;

import com.example.aigument.common.valid.EnumValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValidator.class)
public @interface EnumValidAnnotation {

    String message() default "허용되지 않은 권한입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}