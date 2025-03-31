package com.clokey.server.domain.term.application;

import java.util.List;

import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.term.domain.entity.MemberTerm;
import org.springframework.data.repository.query.Param;


public interface MemberTermRepositoryService {

    void deleteByMemberId(Long memberId);

    List<MemberTerm> findByMember(Member member); // 특정 사용자의 동의한 약관 조회

    void deleteByMemberIdAndTermId(Long memberId, Long termId); // 특정 사용자의 특정 약관 동의 삭제

    MemberTerm save(MemberTerm memberTerm); //// 사용자의 약관 동의 저장

    boolean existsByMemberIdAndTermId(Long memberId, Long termId);

    void deleteAllByMemberIdAndTermId(Long memberId, Long termId);

}
