package com.clokey.server.domain.history.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.model.entity.enums.Visibility;
import org.springframework.transaction.annotation.Transactional;

public interface HistoryRepository extends JpaRepository<History, Long>,HistoryProjectionRepository{


    @Query("SELECT h FROM History h WHERE h.member.id = :memberId AND h.historyDate >= :monthAgo")
    List<History> findHistoriesWithinMonth(@Param("memberId") Long memberId, @Param("monthAgo") LocalDate monthAgo);

    @Transactional
    @Query("UPDATE History h SET h.likes = h.likes + 1 WHERE h.id = :historyId")
    @Modifying(clearAutomatically = true)
    void incrementLikes(Long historyId);

    @Transactional
    @Query("UPDATE History h SET h.likes = h.likes - 1 WHERE h.id = :historyId")
    @Modifying(clearAutomatically = true)
    void decrementLikes(Long historyId);

    @Query("""
    SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END
    FROM History h
    WHERE h.historyDate = :historyDate AND h.member.id = :memberId
""")
    boolean existsByHistoryDateAndMember_Id(LocalDate historyDate, Long memberId);

    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Member m " +
            "LEFT JOIN History h ON m.id = h.member.id AND h.historyDate = :historyDate " +
            "WHERE m.id IN :memberIds " +
            "GROUP BY m.id " +
            "ORDER BY m.id")
    List<Boolean> existsByHistoryDateAndMemberIds(
            @Param("historyDate") LocalDate historyDate,
            @Param("memberIds") List<Long> memberIds);

    @Query("""
    SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END
    FROM History h
    WHERE h.id = :historyId AND h.member.id = :memberId
""")
    boolean checkMyHistory(@Param("historyId") Long historyId,
                           @Param("memberId") Long memberId);


    Optional<History> findByHistoryDateAndMember_Id(LocalDate historyDate, Long memberId);

    @Modifying
    @Query("DELETE FROM History h WHERE h.id IN :historyIds")
    void deleteByHistoryIds(@Param("historyIds") List<Long> historyIds);

    Page<History> findByMemberInAndVisibilityOrderByHistoryDateDesc(List<Member> member, Visibility visibility, Pageable pageable);

    List<History> findTop6ByMemberInAndVisibilityAndHistoryDateAfterOrderByHistoryDateDesc(
            List<Member> members, Visibility visibility, LocalDate startDate);

    @Query("SELECT DISTINCT h FROM History h " +
            "JOIN HashtagHistory hh ON hh.history.id = h.id " +
            "LEFT JOIN Follow f ON f.following.id = h.member.id AND f.followed.id = :currentMemberId " +  // follow 관계 조인
            "WHERE hh.hashtag.id IN :hashtagIds " +
            "AND h.member.id <> :currentMemberId " +
            "AND f.id IS NULL " +  // 팔로우한 사람 제외
            "ORDER BY h.likes DESC, h.historyDate DESC")
    List<History> findTop10MembersByHashtagIdsOrderByLikes(
            @Param("hashtagIds") List<Long> hashtagIds,
            @Param("currentMemberId") Long currentMemberId,
            Pageable pageable);

    List<History> findAll();

    @Query("SELECT COUNT(h) FROM History h WHERE h.member = :member")
    Long countHistoryByMember(@Param("member") Member member);

    @Query("""
    SELECT h FROM History h
    WHERE h.member.id IN :memberIds
      AND h.visibility = 'PUBLIC'
      AND h.banned = false
      AND h.createdAt = (
        SELECT MAX(h2.createdAt)
        FROM History h2
        WHERE h2.member.id = h.member.id
          AND h2.visibility = 'PUBLIC'
          AND h2.banned = false
      )
    """)
    List<History> findHistoryByMemberIdIn(@Param("memberIds") List<Long> memberIds);

    @Query("SELECT h FROM History h WHERE h.member.id IN :memberIds " +
            "AND h.historyDate BETWEEN :from AND :to")
    List<History> findHistoriesByMemberIdsAndDateRange(
            @Param("memberIds") List<Long> memberIds,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
                SELECT h FROM History h
                JOIN MemberLike ml ON h.id = ml.history.id
                WHERE ml.member.id = :memberId
                AND (
                    h.visibility = 'PUBLIC'
                    OR (h.member.id = :memberId AND h.visibility = 'PRIVATE')
                )
                ORDER BY ml.createdAt DESC
            """)
    Page<History> findLikedHistories(@Param("memberId") Long memberId, Pageable pageable);

    @Query("SELECT h FROM History h JOIN FETCH h.member WHERE h.id = :id")
    Optional<History> findByIdWithWriter(@Param("id") Long id);
}
