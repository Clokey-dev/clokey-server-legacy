package com.clokey.server.domain.folder.exception.validator;

import com.clokey.server.domain.folder.domain.repository.FolderRepository;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.folder.exception.annotation.FolderExist;
import com.clokey.server.global.error.code.status.ErrorStatus;

@Component
@RequiredArgsConstructor
public class FolderExistValidator implements ConstraintValidator<FolderExist, Long> {

    private final FolderRepository folderRepository;

    @Override
    public void initialize(FolderExist constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Long folderId, ConstraintValidatorContext context) {
        boolean isValid = folderRepository.existsById(folderId);

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.NO_SUCH_FOLDER.toString()).addConstraintViolation();
        }

        return isValid;

    }
}
