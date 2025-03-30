package com.clokey.server.domain.term.application;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.term.domain.entity.MemberTerm;
import com.clokey.server.domain.term.domain.repository.MemberTermRepository;

@Service
@RequiredArgsConstructor
public class MemberTermRepositoryServiceImpl implements MemberTermRepositoryService {

    private final MemberTermRepository memberTermRepository;

    @Override
    public void deleteByMemberId(Long memberId) {
        memberTermRepository.deleteByMemberId(memberId); // Repository에서 처리
    }

    @Override
    public List<MemberTerm> findByMember(Member member) {
        return memberTermRepository.findByMember(member);
    }

    @Override
    public void deleteByMemberIdAndTermId(Long memberId, Long termId) {
        memberTermRepository.deleteByMemberIdAndTermId(memberId, termId);
    }

    @Override
    public MemberTerm save(MemberTerm memberTerm) {
        return memberTermRepository.save(memberTerm);
    }

    @Override
    public boolean existsByMemberIdAndTermId(Long memberId, Long termId) {
        return memberTermRepository.existsByMemberIdAndTermId(memberId,termId);
    }

    @Override
    public void deleteAllByMemberIdAndTermId(Long memberId, Long termId){
        memberTermRepository.deleteAllByMemberIdAndTermId(memberId, termId);
    }

}
