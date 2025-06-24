package com.clokey.server.domain.notification.exception.validator;

import com.clokey.server.domain.notification.domain.repository.NotificationRepository;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.notification.exception.annotation.NotificationExist;
import com.clokey.server.global.error.code.status.ErrorStatus;

@Component
@RequiredArgsConstructor
public class NotificationExistValidator implements ConstraintValidator<NotificationExist, Long> {

    private final NotificationRepository notificationRepository;

    @Override
    public void initialize(NotificationExist constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Long notificationId, ConstraintValidatorContext context) {
        boolean isValid = notificationRepository.existsById(notificationId);

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.NO_SUCH_NOTIFICATION.toString()).addConstraintViolation();
        }

        return isValid;

    }
}
