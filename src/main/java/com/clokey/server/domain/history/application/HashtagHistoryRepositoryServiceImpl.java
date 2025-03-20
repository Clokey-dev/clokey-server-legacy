package com.clokey.server.domain.history.application;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.history.domain.entity.Hashtag;
import com.clokey.server.domain.history.domain.entity.HashtagHistory;
import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.history.domain.repository.HashtagHistoryRepository;

@Transactional
@Service
@RequiredArgsConstructor
public class HashtagHistoryRepositoryServiceImpl implements HashtagHistoryRepositoryService {

    private final HashtagHistoryRepository hashtagHistoryRepository;

    @Override
    public List<HashtagHistory> findByHistory_Id(Long historyId) {
        return hashtagHistoryRepository.findByHistory_Id(historyId);
    }

    @Override
    public void save(HashtagHistory hashtagHistory) {
        hashtagHistoryRepository.save(hashtagHistory);
    }

    public void addHashtagHistory(Hashtag hashtag, History history) {

        HashtagHistory hashtagHistory = HashtagHistory.builder()
                .hashtag(hashtag)
                .history(history)
                .build();

        hashtagHistoryRepository.save(hashtagHistory);
    }

    public void deleteHashtagHistory(Hashtag hashtag, History history) {
        hashtagHistoryRepository.deleteByHashtagAndHistory(hashtag, history);
    }

    @Override
    public void deleteAllByHistoryId(Long historyId) {
        hashtagHistoryRepository.deleteAllByHistoryId(historyId);
    }

    @Override
    public List<String> findHashtagNamesByHistoryId(Long historyId) {
        return hashtagHistoryRepository.findHashtagNamesByHistoryId(historyId);
    }

    @Override
    public void deleteAllByHistoryIds(List<Long> historyIds) {
        hashtagHistoryRepository.deleteAllByHistoryIds(historyIds);
    }

    public List<Long> findHashtagIdsByMemberIdOrderByHistoryDateDesc(Long memberId) {
        return hashtagHistoryRepository.findHashtagIdsByMemberIdOrderByHistoryDateDesc(memberId);
    }

    @Override
    public String findLatestTaggedHashtag(Long memberId) {
        return hashtagHistoryRepository.findLatestTaggedHashtag(memberId)
                .orElse(null);
    }

    @Override
    public List<HashtagHistory> findTop5HistoriesByHashtagNameOrderByDateDesc(String hashtagName) {
        return hashtagHistoryRepository.findTop5HistoriesByHashtagNameOrderByDateDesc(hashtagName, PageRequest.of(0, 5));
    }

    @Override
    public List<HashtagHistory> findTop5HistoriesByCategoryNameOrderByDateDesc(String categoryName) {
        return hashtagHistoryRepository.findTop5HistoriesByCategoryNameOrderByDateDesc("#"+categoryName, categoryName, PageRequest.of(0, 5));
    }


}
