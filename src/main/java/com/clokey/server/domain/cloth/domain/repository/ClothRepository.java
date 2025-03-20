package com.clokey.server.domain.cloth.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.clokey.server.domain.cloth.domain.entity.Cloth;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.model.entity.enums.Season;
import com.clokey.server.domain.model.entity.enums.Visibility;

public interface ClothRepository extends JpaRepository<Cloth, Long> {

    @EntityGraph(attributePaths = {"image"})
    Optional<Cloth> findById(Long id);

    @Query("SELECT c FROM Cloth c " +
            "JOIN c.member m " +
            "JOIN c.category cat " +
            "WHERE m.clokeyId = :clokeyId " +
            "AND (:categoryId = 0 OR cat.id = :categoryId OR cat.parent.id = :categoryId) " + // 1차 카테고리라면 모든 하위 카테고리 포함
            "AND (:season = 'ALL' OR :season MEMBER OF c.seasons) " + // ALL이면 모든 계절 조회, 아니면 특정 계절만 조회
            "AND (c.visibility = 'PUBLIC' OR m.id = :memberId) " + // 비공개일 경우 본인만 조회 가능
            "ORDER BY " +
            "CASE WHEN :sort = 'WEAR' THEN c.wearNum END DESC, " +
            "CASE WHEN :sort = 'NOT_WEAR' THEN c.wearNum END ASC, " +
            "CASE WHEN :sort = 'OLDEST' THEN c.createdAt END ASC, " +
            "CASE WHEN :sort = 'LATEST' THEN c.createdAt END DESC"
    )
    Page<Cloth> findByClosetFilters(
            @Param("clokeyId") String clokeyId,
            @Param("memberId") Long memberId,
            @Param("categoryId") Long categoryId,
            @Param("season") Season season,
            @Param("sort") String sort,
            Pageable pageable
    );

    @Query("SELECT c FROM Cloth c " +
            "WHERE c.member.id = :memberId " +
            "AND c.category.id = :categoryId " +
            "ORDER BY c.wearNum ASC, c.id ASC LIMIT 3")
    List<Cloth> findLeastFrequentClothList(@Param("memberId") Long memberId, @Param("categoryId") Long categoryId);

    @Query("SELECT c FROM Cloth c " +
            "WHERE c.member.id = :memberId " +
            "AND c.category.id = :categoryId " +
            "ORDER BY c.wearNum DESC, c.id ASC LIMIT 3")
    List<Cloth> findMostFrequentClothList(@Param("memberId") Long memberId, @Param("categoryId") Long categoryId);

    Page<Cloth> findByMemberInAndVisibilityOrderByCreatedAtDesc(
            List<Member> members, Visibility visibility, Pageable pageable);

    List<Cloth> findTop6ByMemberInAndVisibilityAndCreatedAtBetweenOrderByCreatedAtDesc(
            Collection<Member> member, Visibility visibility, LocalDateTime createdAt, LocalDateTime createdAt2);

    @Query("SELECT c FROM Cloth c WHERE c.member.id = :memberId " +
            "AND ((:nowTemp BETWEEN c.tempLowerBound AND c.tempUpperBound) " +
            "OR (c.tempLowerBound <= :maxTemp AND c.tempUpperBound >= :minTemp)) " +
            "ORDER BY RAND()")
    List<Cloth> findBySuitableClothFilters (@Param("memberId") Long memberId,
                                           @Param("nowTemp") Integer nowTemp,
                                           @Param("minTemp") Integer minTemp,
                                           @Param("maxTemp") Integer maxTemp);

    @Modifying
    @Query("DELETE FROM Cloth c WHERE c.id IN :clothIds")
    void deleteByClothIds(@Param("clothIds") List<Long> clothIds);

    @Query("""
        SELECT c
        FROM Cloth c
        WHERE c.member = :member
        ORDER BY c.wearNum DESC
        LIMIT 3
    """)
    List<Cloth> getTop3Cloths(@Param("member") Member member);

    @Query("""
    SELECT c
    FROM Cloth c
    WHERE c.member = :member
      AND c.visibility = 'PUBLIC'
    ORDER BY c.wearNum DESC
    LIMIT 3
""")
    List<Cloth> getTop3PublicCloths(@Param("member") Member member);
}
