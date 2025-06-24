package com.clokey.server.domain.category.exception.validator;

import com.clokey.server.domain.category.domain.repostiory.CategoryRepository;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.category.exception.annotation.CategoryExist;
import com.clokey.server.global.error.code.status.ErrorStatus;

@Component
@RequiredArgsConstructor
public class CategoryExistValidator implements ConstraintValidator<CategoryExist, Long> {

    private final CategoryRepository categoryRepository;

    @Override
    public boolean isValid(Long categoryId, ConstraintValidatorContext context) {
        if(categoryId==0)
            return true;

        boolean isValid = categoryRepository.existsById(categoryId);

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.NO_SUCH_CATEGORY.toString()).addConstraintViolation();
        }

        return isValid;
    }
}
