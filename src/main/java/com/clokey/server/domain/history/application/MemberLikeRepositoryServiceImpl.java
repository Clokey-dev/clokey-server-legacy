package com.clokey.server.domain.history.application;

import org.springframework.stereotype.Service;

import java.util.List;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.history.domain.entity.MemberLike;
import com.clokey.server.domain.history.domain.repository.MemberLikeRepository;
import com.clokey.server.domain.member.domain.entity.Member;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberLikeRepositoryServiceImpl implements MemberLikeRepositoryService {

    private final MemberLikeRepository memberLikeRepository;

    @Override
    public int countByHistory_Id(Long historyId) {
        return memberLikeRepository.countByHistoryId(historyId);
    }

    @Override
    public boolean existsByMember_IdAndHistory_Id(Long memberId, Long historyId) {
        return memberLikeRepository.existsByMemberIdAndHistoryId(memberId, historyId);
    }

    @Override
    public void deleteByMember_IdAndHistory_Id(Long memberId, Long historyId) {
        memberLikeRepository.deleteByMember_IdAndHistory_Id(memberId, historyId);
    }

    @Override
    public void save(MemberLike memberLike) {
        memberLikeRepository.save(memberLike);
    }

    @Override
    public void deleteAllByHistoryId(Long historyId) {
        memberLikeRepository.deleteAllByHistoryId(historyId);
    }

    @Override
    public List<Member> findMembersByHistory(Long historyId) {
        return memberLikeRepository.findMembersByHistoryId(historyId);
    }

    @Override
    public void deleteAllByMemberId(Long memberId) {
        memberLikeRepository.deleteAllByMemberId(memberId);
    }

    @Override
    public void deleteAllByHistoryIds(List<Long> historyIds) {
        memberLikeRepository.deleteAllByHistoryIds(historyIds);
    }
}
