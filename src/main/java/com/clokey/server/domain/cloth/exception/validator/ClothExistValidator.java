package com.clokey.server.domain.cloth.exception.validator;

import com.clokey.server.domain.cloth.domain.repository.ClothRepository;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.cloth.exception.annotation.ClothExist;
import com.clokey.server.global.error.code.status.ErrorStatus;

@Component
@RequiredArgsConstructor
public class ClothExistValidator implements ConstraintValidator<ClothExist, Long> {

    private final ClothRepository clothRepository;

    @Override
    public boolean isValid(Long clothId, ConstraintValidatorContext context) {
        boolean isValid = clothRepository.existsById(clothId);

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.NO_SUCH_CLOTH.toString()).addConstraintViolation();
        }

        return isValid;
    }
}
