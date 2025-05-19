package com.clokey.server.domain.history.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import com.clokey.server.domain.history.domain.entity.MemberLike;
import com.clokey.server.domain.member.domain.entity.Member;

public interface MemberLikeRepository extends JpaRepository<MemberLike, Long> {

    @Query("SELECT COUNT(h) FROM HistoryCloth h WHERE h.history.id = :historyId")
    int countByHistoryId(@Param("historyId") Long historyId);

    @Query("""
    SELECT CASE WHEN COUNT(ml) > 0 THEN true ELSE false END
    FROM MemberLike ml
    WHERE ml.member.id = :memberId
      AND ml.history.id = :historyId
""")
    boolean existsByMemberIdAndHistoryId(@Param("memberId") Long memberId,
                                         @Param("historyId") Long historyId);
    @Modifying
    @Query("""
    DELETE FROM MemberLike ml
    WHERE ml.member.id = :memberId AND ml.history.id = :historyId
""")
    void deleteByMemberIdAndHistoryId(@Param("memberId") Long memberId,
                                      @Param("historyId") Long historyId);

    @Modifying  // 수정/삭제 작업을 나타냄
    @Query("DELETE FROM MemberLike ml WHERE ml.history.id = :historyId")
    void deleteAllByHistoryId(@Param("historyId") Long historyId);

    @Query("SELECT ml.member FROM MemberLike ml WHERE ml.history.id = :historyId")
    List<Member> findMembersByHistoryId(@Param("historyId") Long historyId);

    @Modifying
    @Query("DELETE FROM MemberLike ml WHERE ml.member.id = :memberId")
    void deleteAllByMemberId(@Param("memberId") Long memberId);

    @Modifying
    @Query("DELETE FROM MemberLike ml WHERE ml.history.id IN :historyIds")
    void deleteAllByHistoryIds(@Param("historyIds") List<Long> historyIds);

    //for test
    boolean existsByHistoryId(Long historyId);
}
