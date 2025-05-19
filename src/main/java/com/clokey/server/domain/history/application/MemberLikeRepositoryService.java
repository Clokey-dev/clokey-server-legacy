package com.clokey.server.domain.history.application;

import java.util.List;

import com.clokey.server.domain.history.domain.entity.MemberLike;
import com.clokey.server.domain.member.domain.entity.Member;

public interface MemberLikeRepositoryService {

    int countByHistory_Id(Long historyId);

    boolean existsByMember_IdAndHistory_Id(Long memberId, Long historyId);

    void deleteByMemberIdAndHistoryId(Long memberId, Long historyId);

    void save(MemberLike memberLike);

    void deleteAllByHistoryId(Long historyId);

    List<Member> findMembersByHistory(Long historyId);

    void deleteAllByMemberId(Long memberId);

    void deleteAllByHistoryIds(List<Long> historyIds);
}
