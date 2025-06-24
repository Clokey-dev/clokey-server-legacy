package com.clokey.server.domain.member.application;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.model.entity.enums.SocialType;

public interface MemberRepositoryService {
    boolean memberExist(Long memberId);

    Member findMemberById(Long memberId);

    Member saveMember(Member member);

    Optional<Member> getMember(Long memberId);

    boolean idExist(String clokeyId);

    Member findMemberByClokeyId(String clokeyId);

    Member getReferencedById(Long memberId);

    boolean existsByClokeyId(String clokeyId);

    Member findByClokeyId(String clokeyId);

    Optional<Member> findMemberByEmail(String email);

    Member getMemberByEmailAndSocialType(String email, SocialType socialType);

    boolean existsByEmailAndSocialType(String email, SocialType socialType);

    List<Member> findInactiveUsersBefore(LocalDate cutoffDate);

    List<Long> findHistoryIdsByMemberId(Long memberId);

    List<Long> findClothIdsByMemberId(Long memberId);

    List<Long> findFolderIdsByMemberId(Long memberId);

    List<Long> findCommentIdsByMemberId(Long memberId);

    List<Long> findNotificationIdsByMemberId(Long memberId);

    void deleteMemberById(Long memberId);

    List<Member> findAll();

    Map<Long, Member> findMembersByIds(Set<Long> memberIds);
}
