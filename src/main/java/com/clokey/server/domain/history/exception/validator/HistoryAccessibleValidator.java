package com.clokey.server.domain.history.exception.validator;

import com.clokey.server.domain.history.domain.repository.HistoryRepository;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.member.domain.repository.MemberRepository;
import com.clokey.server.domain.member.exception.MemberException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.history.exception.HistoryException;
import com.clokey.server.domain.model.entity.enums.Visibility;
import com.clokey.server.global.error.code.status.ErrorStatus;

@Component
@RequiredArgsConstructor
public class HistoryAccessibleValidator {

    private final HistoryRepository historyRepository;
    private final MemberRepository memberRepository;

    public void validateHistoryAccessOfMember(Long historyId, Long memberId) {
        History history = historyRepository.findByIdWithWriter(historyId).orElseThrow(()-> new HistoryException(ErrorStatus.NO_SUCH_HISTORY));
        Member memberRequestedAccess = memberRepository.findById(memberId).orElseThrow(()-> new MemberException(ErrorStatus.NO_SUCH_MEMBER));

        //접근 권한 확인 - 나의 기록이 아니고 비공개일 경우 접근 불가.
        boolean isPrivate = history.getVisibility().equals(Visibility.PRIVATE);
        boolean isNotMyHistory = !history.getMember().equals(memberRequestedAccess);

        if (isPrivate && isNotMyHistory) {
            throw new HistoryException(ErrorStatus.NO_PERMISSION_TO_ACCESS_HISTORY);
        }

        //접근 권한 확인 - 나의 기록이 아니고 기록의 주인이 비공개일 경우 접근 불가.
        Member writer = history.getMember();

        boolean writerIsPrivate = writer.getVisibility().equals(Visibility.PRIVATE);
        if(writerIsPrivate && isNotMyHistory){
            throw new HistoryException(ErrorStatus.NO_PERMISSION_TO_ACCESS_HISTORY);
        }
    }

    public void validateMemberAccessOfMember(Long memberToBeQueried, Long memberRequestingQuery) {

        //접근 권한 확인 - 내 자신을 확인하는 것도 아니고 비공개인 경우.
        boolean selfQuery = memberToBeQueried.equals(memberRequestingQuery);
        boolean isPrivate = memberRepository.findMemberById(memberToBeQueried)
                .orElseThrow(()-> new MemberException(ErrorStatus.NO_SUCH_MEMBER))
                .getVisibility()
                .equals(Visibility.PRIVATE);

        if (!selfQuery && isPrivate) {
            throw new HistoryException(ErrorStatus.NO_PERMISSION_TO_ACCESS_HISTORY);
        }

    }

    public void validateMyHistory(Long historyId, Long MemberId) {

        boolean isValid = historyRepository.findById(historyId)
                .orElseThrow(()-> new HistoryException(ErrorStatus.NO_SUCH_HISTORY))
                .getMember()
                .getId()
                .equals(MemberId);

        if (!isValid) {
            throw new HistoryException(ErrorStatus.NOT_MY_HISTORY);
        }
    }

}
