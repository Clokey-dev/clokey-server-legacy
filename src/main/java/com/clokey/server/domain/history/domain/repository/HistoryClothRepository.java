package com.clokey.server.domain.history.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import com.clokey.server.domain.cloth.domain.entity.Cloth;
import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.history.domain.entity.HistoryCloth;

public interface HistoryClothRepository extends JpaRepository<HistoryCloth, Long> {

    void deleteAllByClothId(Long clothId);

    @Transactional
    @Modifying
    @Query("DELETE FROM HistoryCloth hc WHERE hc.history = :history AND hc.cloth = :cloth")
    void deleteByHistoryAndCloth(@Param("history") History history, @Param("cloth") Cloth cloth);


    @Query("SELECT hc.cloth.id FROM HistoryCloth hc WHERE hc.history.id = :historyId")
    List<Long> findClothIdsByHistoryId(@Param("historyId") Long historyId);

    // 특정 HistoryId에 연결된 모든 Cloth 조회
    @Query("SELECT hc.cloth FROM HistoryCloth hc WHERE hc.history.id = :historyId")
    List<Cloth> findAllClothsByHistoryId(@Param("historyId") Long historyId);

    // 특정 HistoryId에 연결된 모든 HistoryClothResult 삭제
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM HistoryCloth hc WHERE hc.history.id = :historyId")
    void deleteAllByHistoryId(@Param("historyId") Long historyId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM HistoryCloth hc WHERE hc.history.id IN :historyIds")
    void deleteAllByHistoryIds(@Param("historyIds") List<Long> historyIds);

    // 최적화 여부 판단 필요 -> 테스트 코드 작성 안함.
    @Query("SELECT c.cloth.category.name FROM HistoryCloth c WHERE c.cloth.member.id = :memberId " +
            "GROUP BY c.cloth.category.name ORDER BY COUNT(c.id) DESC LIMIT 1")
    Optional<String> findMostWornCategory(@Param("memberId") Long memberId);

    //for test
    boolean existsByHistoryIdAndClothId(Long historyId, Long clothId);
    boolean existsByHistoryId(Long historyId);
    boolean existsByClothId(Long clothId);

}
