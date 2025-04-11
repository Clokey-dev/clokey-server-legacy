package com.clokey.server.domain.member.domain.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.clokey.server.domain.member.domain.entity.Follow;
import com.clokey.server.domain.member.domain.entity.Member;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowing_IdAndFollowed_Id(Long followingId, Long followedId);

    @Query("SELECT m, CASE WHEN EXISTS (SELECT 1 FROM Follow f WHERE f.following = m AND f.followed.id = :followedId) " +
            "THEN true ELSE false END FROM Member m WHERE m IN :members")
    List<Object[]> findFollowingStatus(@Param("followedId") Long followedId, @Param("members") List<Member> members);

    @Query("SELECT CASE WHEN EXISTS (SELECT 1 FROM Follow f WHERE f.followed = m AND f.following.id = :followingId) THEN true ELSE false END " +
            "FROM Member m WHERE m IN :members")
    List<Boolean> checkFollowedStatus(@Param("followingId") Long followingId, @Param("members") List<Member> members);

    Optional<Follow> findByFollowing_IdAndFollowed_Id(Long followingId, Long followedId);

    @Query("SELECT f.followed FROM Follow f WHERE f.following.id = :followingId")
    List<Member> findFollowedByFollowingId(@Param("followingId") Long followingId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Follow f WHERE f.following.id = :memberId OR f.followed.id = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT f.following FROM Follow f WHERE f.followed.id = :followedId")
    List<Member> findFollowingByFollowedId(@Param("followedId") Long followedId);

    @Query("SELECT COUNT(f) > 0 FROM Follow f WHERE f.following = :targetUser AND f.followed = :currentUser")
    boolean isFollowing(@Param("currentUser") Member currentUser, @Param("targetUser") Member targetUser);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.following = :member")
    Long countFollowersByMember(@Param("member") Member member);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.followed = :member")
    Long countFollowingByMember(@Param("member") Member member);

    @Query("SELECT f.followed FROM Follow f WHERE f.following.id = :followingId")
    List<Member> findFollowedByFollowingId(@Param("followingId") Long followingId, Pageable pageable);

    @Query("SELECT f.following FROM Follow f WHERE f.followed.id = :followedId")
    List<Member> findFollowingByFollowedId(@Param("followedId") Long followedId, Pageable pageable);

    @Query("""
    SELECT f.following.id FROM Follow f
    WHERE f.following.id NOT IN :blockingMemberIds
      AND f.following.id <> :memberId
      AND f.following.banned = false
      AND f.following.visibility = 'PUBLIC'
      AND EXISTS (
        SELECT 1 FROM History h
        WHERE h.member.id = f.following.id
          AND h.visibility = 'PUBLIC'
          AND h.banned = false
      )
    GROUP BY f.following.id
    ORDER BY COUNT(f.followed.id) DESC
    """)
    List<Long> findTopFollowingMembers(Set<Long> blockingMemberIds, Long memberId, Pageable pageable);
}
