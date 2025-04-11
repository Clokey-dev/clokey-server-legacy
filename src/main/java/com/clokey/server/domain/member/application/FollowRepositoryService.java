package com.clokey.server.domain.member.application;


import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.clokey.server.domain.member.domain.entity.Follow;
import com.clokey.server.domain.member.domain.entity.Member;

public interface FollowRepositoryService {

    List<Boolean> checkFollowingStatus(Long followedId, List<Member> members);

    List<Boolean> checkFollowedStatus(Long followedId, List<Member> members);

    List<Member> findFollowedByFollowingId(Long followingId);

    void deleteByMemberId(Long memberId);

    boolean existsByFollowing_IdAndFollowed_Id(Long followingId, Long followedId);

    List<Member> findFollowingByFollowedId(Long followedId);

    Optional<Follow> findByFollowing_IdAndFollowed_Id(Long followingId, Long followedId);

    void delete(Follow follow);

    void save(Follow follow);

    boolean isFollowing(Member currentUser, Member targetUser);

    Long countFollowersByMember(Member member);

    Long countFollowingByMember(Member member);

    List<Member> findFollowedByFollowingId(Long followingId, Pageable pageable);

    List<Member> findFollowingByFollowedId(Long followedId, Pageable pageable);

    List<Long> findTopFollowingMembers(Set<Long> blockingMemberIds, Member member);
}
