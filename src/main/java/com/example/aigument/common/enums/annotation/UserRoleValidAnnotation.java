package com.example.aigument.common.enums.annotation;

import com.example.aigument.common.enums.valid.UserRoleValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UserRoleValidator.class)
public @interface UserRoleValidAnnotation {

    String message() default "허용되지 않은 권한입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}