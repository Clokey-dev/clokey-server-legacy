package com.clokey.server.domain.history.exception.validator;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.history.exception.annotation.HashtagCondition;
import com.clokey.server.global.error.code.status.ErrorStatus;

@Component
@RequiredArgsConstructor
public class HashtagConditionValidator implements ConstraintValidator<HashtagCondition, List<String>> {

    public static final int MAXIMUM_HASHTAGS = 20;

    @Override
    public void initialize(HashtagCondition constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(List<String> hashtags, ConstraintValidatorContext context) {

        if (hashtags == null || hashtags.isEmpty()) {
            return true; // 비어있는 경우는 유효하다고 판단
        }

        if (hashtags.size() > MAXIMUM_HASHTAGS){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.TOO_MANY_HASHTAGS.toString()).addConstraintViolation();

            return false;
        }

        Set<String> uniqueHashtags = new HashSet<>(hashtags);
        if (uniqueHashtags.size() != hashtags.size()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.DUPLICATE_HASHTAGS.toString()).addConstraintViolation();

            return false;
        }

        return true;

    }
}
