package com.clokey.server.domain.member.exception.validator;

import com.clokey.server.domain.member.application.MemberRepositoryService;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.member.dto.MemberDTO;
import com.clokey.server.domain.member.exception.annotation.NullableClokeyIdExist;
import com.clokey.server.domain.member.exception.annotation.VisualizeBannedUser;
import com.clokey.server.domain.model.entity.enums.Visibility;
import com.clokey.server.global.error.code.status.ErrorStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VisualizeBannedUserValidator implements ConstraintValidator<VisualizeBannedUser, MemberDTO.ProfileRQ> {

    private final MemberRepositoryService memberRepositoryService;

    @Override
    public void initialize(VisualizeBannedUser constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(MemberDTO.ProfileRQ request, ConstraintValidatorContext context) {

        boolean banned = memberRepositoryService.findByClokeyId(request.getClokeyId()).isBanned();
        boolean changToPublic = request.getVisibility().equals(Visibility.PUBLIC);

        if (banned && changToPublic) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.BANNED_MEMBER_TO_PUBLIC.toString()).addConstraintViolation();
        }

        return true;

    }
}

