package com.clokey.server.domain.history.exception.annotation;

import java.lang.annotation.*;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import com.clokey.server.domain.history.exception.validator.HashtagConditionValidator;

@Documented
@Constraint(validatedBy = HashtagConditionValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface HashtagCondition {

    String message() default "해시태그 등록 조건을 위배했습니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
