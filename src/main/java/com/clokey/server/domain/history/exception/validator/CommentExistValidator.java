package com.clokey.server.domain.history.exception.validator;

import com.clokey.server.domain.history.domain.repository.CommentRepository;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.history.exception.annotation.CommentExist;
import com.clokey.server.global.error.code.status.ErrorStatus;

@Component
@RequiredArgsConstructor
public class CommentExistValidator implements ConstraintValidator<CommentExist, Long> {

    private final CommentRepository commentRepository;

    @Override
    public void initialize(CommentExist constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Long commentId, ConstraintValidatorContext context) {
        boolean isValid = commentRepository.existsById(commentId);

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.NO_SUCH_COMMENT.toString()).addConstraintViolation();
        }

        return isValid;

    }
}
