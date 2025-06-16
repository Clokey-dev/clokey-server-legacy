package com.clokey.server.domain.history.exception.validator;

import com.clokey.server.domain.history.domain.repository.CommentRepository;
import com.clokey.server.domain.history.exception.HistoryException;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.history.domain.entity.Comment;
import com.clokey.server.domain.history.exception.annotation.ParentCommentConditions;
import com.clokey.server.global.error.code.status.ErrorStatus;

@Component
@RequiredArgsConstructor
public class ParentCommentsConditionsValidator implements ConstraintValidator<ParentCommentConditions, Long> {

    private final CommentRepository commentRepository;

    @Override
    public void initialize(ParentCommentConditions constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Long commentId, ConstraintValidatorContext context) {

        //null인 경우 검증 통과
        if (commentId == null) {
            return true;
        }

        //null이 아닌 경우 존재를 확인함.
        if (!commentRepository.existsById(commentId)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.NO_SUCH_COMMENT.toString()).addConstraintViolation();

            return false;
        }

        //존재하는 경우 댓글 대댓글 깊이 검사
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(()->new HistoryException(ErrorStatus.NO_SUCH_COMMENT))
                .getComment();

        if (parentComment != null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.NESTED_COMMENT.toString()).addConstraintViolation();

            return false;
        }

        return true;
    }
}
