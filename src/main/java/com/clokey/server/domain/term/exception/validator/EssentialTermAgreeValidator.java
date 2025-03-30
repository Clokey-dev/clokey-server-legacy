package com.clokey.server.domain.term.exception.validator;

import com.clokey.server.domain.term.application.TermRepositoryService;
import com.clokey.server.domain.term.domain.entity.Term;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.term.dto.TermRequestDTO;
import com.clokey.server.domain.term.exception.annotation.EssentialTermAgree;
import com.clokey.server.global.error.code.status.ErrorStatus;

@Component
@RequiredArgsConstructor
public class EssentialTermAgreeValidator implements ConstraintValidator<EssentialTermAgree, TermRequestDTO.Join> {

    private final TermRepositoryService termRepositoryService;

    @Override
    public void initialize(EssentialTermAgree constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(TermRequestDTO.Join joinRequest, ConstraintValidatorContext context) {
        if (joinRequest == null || joinRequest.getTerms() == null || joinRequest.getTerms().isEmpty()) {
            return false; // 약관 리스트가 비어있으면 유효하지 않음
        }

        // 필수 약관이 동의되지 않았는지 확인
        boolean allEssentialAgreed = joinRequest.getTerms().stream()
                .allMatch(termDto -> {
                    if (termDto.getTermId() == null) return true;

                    Term term = termRepositoryService.findById(termDto.getTermId());

                    // 필수 약관인데 동의하지 않았다면 실패
                    if (!term.getOptional() && Boolean.FALSE.equals(termDto.getAgreed())) {
                        return false;
                    }
                    return true;
                });

        if (!allEssentialAgreed) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.ESSENTIAL_TERM_NOT_AGREED.toString()).addConstraintViolation();
        }
        return allEssentialAgreed;
    }
}
