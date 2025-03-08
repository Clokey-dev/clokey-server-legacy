package com.clokey.server.domain.report.exception.annotation;

import com.clokey.server.domain.history.exception.validator.CommentExistValidator;
import com.clokey.server.domain.report.exception.validator.ReportLengthValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ReportLengthValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ReportLength {

    String message() default "신고의 내용이 너무 깁니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

