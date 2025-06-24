package com.clokey.server.domain.term.exception.validator;

import com.clokey.server.domain.term.domain.repository.TermRepository;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.term.dto.TermRequestDTO;
import com.clokey.server.domain.term.exception.annotation.InvalidTermId;
import com.clokey.server.global.error.code.status.ErrorStatus;

@Component
@RequiredArgsConstructor
public class InvalidTermIdValidator implements ConstraintValidator<InvalidTermId, TermRequestDTO.Join> {

    private final TermRepository termRepository;

    @Override
    public void initialize(InvalidTermId constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(TermRequestDTO.Join joinRequest, ConstraintValidatorContext context) {
        if (joinRequest == null || joinRequest.getTerms() == null || joinRequest.getTerms().isEmpty()) {
            return false; // 약관 리스트가 비어있으면 유효하지 않음
        }

        // 요청된 약관 ID가 실제 존재하는지 확인
        boolean allTermsExist = joinRequest.getTerms().stream()
                .allMatch(term -> term.getTermId() != null && termRepository.existsById(term.getTermId()));

        if (!allTermsExist) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.INVALID_TERM_ID.toString()).addConstraintViolation();
        }

        return allTermsExist;
    }
}
