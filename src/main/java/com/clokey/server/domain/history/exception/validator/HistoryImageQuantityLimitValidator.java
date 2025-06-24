package com.clokey.server.domain.history.exception.validator;

import com.clokey.server.domain.history.domain.repository.HistoryRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.history.exception.annotation.HistoryImageQuantityLimit;
import com.clokey.server.global.error.code.status.ErrorStatus;

@Component
@RequiredArgsConstructor
public class HistoryImageQuantityLimitValidator implements ConstraintValidator<HistoryImageQuantityLimit, List<MultipartFile>> {

    private final HistoryRepository historyRepository;

    @Override
    public void initialize(HistoryImageQuantityLimit constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(List<MultipartFile> images, ConstraintValidatorContext context) {

        boolean isValid = images != null && !images.isEmpty() && images.size() <= 10;

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.IMAGE_QUANTITY_OVER_HISTORY_IMAGE_LIMIT.toString()).addConstraintViolation();
        }

        return isValid;

    }
}
