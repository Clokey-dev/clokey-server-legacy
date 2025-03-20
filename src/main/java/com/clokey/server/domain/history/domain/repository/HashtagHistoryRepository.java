package com.clokey.server.domain.history.domain.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import jakarta.transaction.Transactional;

import com.clokey.server.domain.history.domain.entity.Hashtag;
import com.clokey.server.domain.history.domain.entity.HashtagHistory;
import com.clokey.server.domain.history.domain.entity.History;

public interface HashtagHistoryRepository extends JpaRepository<HashtagHistory, Long> {

    boolean existsByHistory_Id(Long historyId);

    List<HashtagHistory> findByHistory_Id(Long historyId);

    @Query("SELECT h.name FROM HashtagHistory hh JOIN hh.hashtag h WHERE hh.history.id = :historyId")
    List<String> findHashtagNamesByHistoryId(@Param("historyId") Long historyId);

    @Transactional
    @Modifying
    @Query("DELETE FROM HashtagHistory hh WHERE hh.hashtag = :hashtag AND hh.history = :history")
    void deleteByHashtagAndHistory(@Param("hashtag") Hashtag hashtag, @Param("history") History history);

    @Transactional
    @Modifying
    @Query("DELETE FROM HashtagHistory hh WHERE hh.history.id = :historyId")
    void deleteAllByHistoryId(@Param("historyId") Long historyId);

    @Modifying
    @Transactional
    @Query("DELETE FROM HashtagHistory hh WHERE hh.history.id IN :historyIds")
    void deleteAllByHistoryIds(@Param("historyIds") List<Long> historyIds);

    @Query("SELECT hh.hashtag.id FROM HashtagHistory hh " +
            "JOIN hh.history h " +
            "WHERE h.member.id = :memberId " +
            "ORDER BY h.member.clokeyId DESC " +
            "LIMIT 10")
    List<Long> findHashtagIdsByMemberIdOrderByHistoryDateDesc(@Param("memberId") Long memberId);

    @Query("SELECT hh.hashtag.name FROM HashtagHistory hh " +
            "JOIN hh.history h WHERE h.member.id = :memberId " +
            "ORDER BY h.historyDate DESC LIMIT 1")
    Optional<String> findLatestTaggedHashtag(@Param("memberId") Long memberId);

    @Query("SELECT hh FROM HashtagHistory hh WHERE hh.hashtag.name = :hashtagName ORDER BY hh.history.createdAt DESC")
    List<HashtagHistory> findTop5HistoriesByHashtagNameOrderByDateDesc(@Param("hashtagName") String hashtagName, Pageable pageable);

    @Query("SELECT hh FROM HashtagHistory hh " +
            "JOIN hh.history h " +
            "JOIN HistoryCloth hc ON hc.history = h " +
            "JOIN hc.cloth c " +
            "JOIN c.category ch " +
            "WHERE hh.hashtag.name = :hashtagName OR ch.name = :categoryName " +
            "ORDER BY h.createdAt DESC")
    List<HashtagHistory> findTop5HistoriesByCategoryNameOrderByDateDesc(
            @Param("hashtagName") String hashtagName,
            @Param("categoryName") String categoryName,
            Pageable pageable);

}
