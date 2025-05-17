package com.clokey.server.domain.history.exception.validator;

import com.clokey.server.domain.history.dto.projection.HistoryAccessCheckProjectionDTO;
import com.clokey.server.domain.member.domain.entity.Member;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.history.application.HistoryRepositoryService;
import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.history.exception.HistoryException;
import com.clokey.server.domain.member.application.MemberRepositoryService;
import com.clokey.server.domain.model.entity.enums.Visibility;
import com.clokey.server.global.error.code.status.ErrorStatus;

@Component
@RequiredArgsConstructor
public class HistoryAccessibleValidator {

    private final HistoryRepositoryService historyRepositoryService;
    private final MemberRepositoryService memberRepositoryService;

    public void validateHistoryAccessOfMember(Long historyId, Long memberId) {
        HistoryAccessCheckProjectionDTO accessInfo = historyRepositoryService.findAccessInfoByHistoryId(historyId);

        //접근 권한 확인 - 기록의 주인이 비공개일 경우 접근 불가.
        Member writer = memberRepositoryService.findMemberById(accessInfo.getWriterId());
        if(writer.getVisibility().equals(Visibility.PRIVATE)){
            throw new HistoryException(ErrorStatus.NO_PERMISSION_TO_ACCESS_HISTORY);
        }

        //접근 권한 확인 - 나의 기록이 아니고 비공개일 경우 접근 불가.
        boolean isPrivate = accessInfo.getVisibility().equals(Visibility.PRIVATE);
        boolean isNotMyHistory = !accessInfo.getWriterId().equals(memberId);

        if (isPrivate && isNotMyHistory) {
            throw new HistoryException(ErrorStatus.NO_PERMISSION_TO_ACCESS_HISTORY);
        }
    }

    public void validateMemberAccessOfMember(Long memberToBeQueried, Long memberRequestingQuery) {

        //접근 권한 확인 - 내 자신을 확인하는 것도 아니고 비공개인 경우.
        boolean selfQuery = memberToBeQueried.equals(memberRequestingQuery);
        boolean isPrivate = memberRepositoryService.findMemberById(memberToBeQueried)
                .getVisibility()
                .equals(Visibility.PRIVATE);

        if (!selfQuery && isPrivate) {
            throw new HistoryException(ErrorStatus.NO_PERMISSION_TO_ACCESS_HISTORY);
        }

    }

    public void validateMyHistory(Long historyId, Long MemberId) {

        boolean isValid = historyRepositoryService.findById(historyId)
                .getMember()
                .getId()
                .equals(MemberId);

        if (!isValid) {
            throw new HistoryException(ErrorStatus.NOT_MY_HISTORY);
        }
    }

}
