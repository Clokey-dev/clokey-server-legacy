package com.clokey.server.domain.history.exception.validator;

import com.clokey.server.domain.history.domain.repository.MemberLikeRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.history.application.MemberLikeRepositoryService;
import com.clokey.server.domain.history.exception.HistoryException;
import com.clokey.server.global.error.code.status.ErrorStatus;

@Component
@RequiredArgsConstructor
public class HistoryLikedValidator {

    private final MemberLikeRepository memberLikeRepository;

    public void validateIsLiked(Long historyId, Long memberId, boolean isLiked) {

        boolean isValid = memberLikeRepository.existsByMemberIdAndHistoryId(memberId, historyId) == isLiked;

        if (!isValid) {
            throw new HistoryException(ErrorStatus.IS_LIKED_INVALID);
        }
    }

}
