package com.clokey.server.domain.search.exception.validator;

import com.clokey.server.domain.history.domain.repository.CommentRepository;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.search.exception.annotation.KeywordNotNull;
import com.clokey.server.global.error.code.status.ErrorStatus;

@Component
@RequiredArgsConstructor
public class KeywordNotNullValidator implements ConstraintValidator<KeywordNotNull, String> {

    private final CommentRepository commentRepository;

    @Override
    public void initialize(KeywordNotNull constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String keyword, ConstraintValidatorContext context) {

        //null인 경우 검증 실패
        if(keyword == null||keyword.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.NO_SUCH_PARAMETER.toString()).addConstraintViolation();

            return false;
        }

        return true;
    }
}
