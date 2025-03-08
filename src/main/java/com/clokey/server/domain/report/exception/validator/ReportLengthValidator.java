package com.clokey.server.domain.report.exception.validator;

import com.clokey.server.domain.history.exception.annotation.CommentContentLength;
import com.clokey.server.domain.report.exception.annotation.ReportLength;
import com.clokey.server.global.error.code.status.ErrorStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportLengthValidator implements ConstraintValidator<ReportLength, String> {

    private final int REPORT_CONTENT_LENGTH = 200;

    @Override
    public void initialize(ReportLength constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String content, ConstraintValidatorContext context) {
        boolean isValid = content.length() <= REPORT_CONTENT_LENGTH;

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(String.format(ErrorStatus.REPORT_OUT_OF_RANGE.getMessage(),REPORT_CONTENT_LENGTH)).addConstraintViolation();
        }

        return isValid;

    }
}
