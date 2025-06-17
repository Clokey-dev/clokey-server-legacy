package com.clokey.server.domain.history.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import com.clokey.server.domain.history.domain.entity.HistoryImage;
import org.springframework.transaction.annotation.Transactional;

public interface HistoryImageRepository extends JpaRepository<HistoryImage, Long> {

    List<HistoryImage> findByHistory_Id(Long historyId);

    @Query("SELECT hi.imageUrl FROM HistoryImage hi WHERE hi.history.id = :historyId ORDER BY hi.createdAt ASC")
    List<String> getImageUrlsByHistoryIdOrderByCreatedAtAsc(@Param("historyId") Long historyId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM HistoryImage hi WHERE hi.history.id IN :historyIds")
    void deleteAllByHistoryIds(@Param("historyIds") List<Long> historyIds);

    List<HistoryImage> findByHistoryIdIn(List<Long> historyIds);

    //for test
    boolean existsByHistoryId(Long historyId);

    @Query(value = """
    SELECT history_id, image_url
    FROM (
        SELECT *,
               ROW_NUMBER() OVER (PARTITION BY history_id ORDER BY created_at ASC) AS rn
        FROM history_image
        WHERE history_id IN :historyIds
    ) sub
    WHERE rn = 1
""", nativeQuery = true)
    List<Object[]> getFirstImageUrlsWithHistoryId(@Param("historyIds") List<Long> historyIds);
}
