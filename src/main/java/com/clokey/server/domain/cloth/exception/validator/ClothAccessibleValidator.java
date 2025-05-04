package com.clokey.server.domain.cloth.exception.validator;

import org.springframework.stereotype.Component;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.cloth.application.ClothRepositoryService;
import com.clokey.server.domain.cloth.domain.entity.Cloth;
import com.clokey.server.domain.cloth.exception.ClothException;
import com.clokey.server.domain.member.application.MemberRepositoryService;
import com.clokey.server.domain.model.entity.enums.Visibility;
import com.clokey.server.global.error.code.status.ErrorStatus;

@Component
@RequiredArgsConstructor
public class ClothAccessibleValidator {

    private final MemberRepositoryService memberRepositoryService;
    private final ClothRepositoryService clothRepositoryService;

    // 유저가 옷에 대한 접근 권한이 있는지 검증 -> 비공개 옷을 조회하지 못하도록 함
    public void validateClothAccessOfMember(Long clothId, Long memberId) {
        Cloth cloth = clothRepositoryService.findById(clothId);

        //접근 권한 확인 - 나의 옷이 아니고 비공개일 경우 접근 불가.
        boolean isPublic = cloth.getVisibility().equals(Visibility.PUBLIC);
        boolean isNotMyCloth = !cloth.getMemberId().equals(memberId);

        if (!isPublic && isNotMyCloth) {
            throw new ClothException(ErrorStatus.NO_PERMISSION_TO_ACCESS_CLOTH);
        }
    }

    // 유저가 옷에 대한 수정권한이 있는지 검증 (내옷 인지 확인)
    public void validateClothOfMember(Long clothId, Long memberId) {
        Cloth cloth = clothRepositoryService.findById(clothId);

        //내 옷이 아닌지 확인
        boolean isNotMyCloth = !cloth.getMemberId().equals(memberId);

        if (isNotMyCloth) {
            throw new ClothException(ErrorStatus.NOT_MY_CLOTH);
        }
    }

    //다른 입력 인자로 오버로딩
    public void validateClothOfMember(List<Long> clothIds, Long memberId) {

        List<Long> clothOwners = clothRepositoryService.getClothOwners(clothIds);
        boolean hasInvalidOwner = clothOwners.stream()
                .anyMatch(ownerId -> !ownerId.equals(memberId));

        if (hasInvalidOwner) {
            throw new ClothException(ErrorStatus.NOT_MY_CLOTH);
        }
    }

    // 유저가 다른 유저의 옷을 조회하려할 때를 검증 -> 비공개 유저의 옷을 조회하지 못하도록 함
    public void validateMemberAccessOfMemberByCloth (Long clothId, Long requesterId) {
        Cloth cloth = clothRepositoryService.findById(clothId);
        Long ownerId = cloth.getMemberId();

        //접근 권한 확인 - 내 자신을 확인하는 것도 아니고 비공개인 경우.
        boolean selfQuery = ownerId.equals(requesterId);
        boolean isOwnerPrivate = memberRepositoryService.getReferencedById(ownerId)
                .getVisibility()
                .equals(Visibility.PRIVATE);

        if(!selfQuery && isOwnerPrivate) {
            throw new ClothException(ErrorStatus.NO_PERMISSION_TO_ACCESS_CLOTH);
        }
    }

}
