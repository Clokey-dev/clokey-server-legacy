package com.clokey.server.domain.term.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.term.domain.entity.MemberTerm;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberTermRepository extends JpaRepository<MemberTerm, Long> {
    void deleteByMemberId(Long memberId);

    List<MemberTerm> findByMember(Member member); // 특정 사용자의 동의한 약관 조회

    void deleteByMemberIdAndTermId(Long memberId, Long termId); // 특정 사용자의 특정 약관 동의 삭제

    MemberTerm save(MemberTerm memberTerm); // 사용자의 약관 동의 저장

    boolean existsByMemberIdAndTermId(Long memberId, Long termId);

    @Modifying
    @Query("DELETE FROM MemberTerm mt WHERE mt.member.id = :memberId AND mt.term.id = :termId")
    void deleteAllByMemberIdAndTermId(@Param("memberId") Long memberId, @Param("termId") Long termId);

}
