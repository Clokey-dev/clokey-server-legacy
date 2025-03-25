package com.clokey.server.domain.history.application;

import java.util.List;

import com.clokey.server.domain.history.domain.entity.Hashtag;
import com.clokey.server.domain.history.domain.entity.HashtagHistory;
import com.clokey.server.domain.history.domain.entity.History;

public interface HashtagHistoryRepositoryService {

    List<HashtagHistory> findByHistory_Id(Long historyId);

    void save(HashtagHistory hashtagHistory);

    void addHashtagHistory(Hashtag hashtag, History history);

    void deleteHashtagHistory(Hashtag hashtag, History history);

    void deleteAllByHistoryId(Long historyId);

    List<String> findHashtagNamesByHistoryId(Long historyId);

    void deleteAllByHistoryIds(List<Long> historyIds);

    List<Long> findHashtagIdsByMemberIdOrderByHistoryDateDesc(Long memberId);

    String findLatestTaggedHashtag(Long memberId);

    List<HashtagHistory> findTop5HistoriesByHashtagNameOrderByDateDesc(String hashtagName);

    List<HashtagHistory> findTop5HistoriesByCategoryNameOrderByDateDesc(String categoryName);

}
