package com.clokey.server.domain.history.application;

import java.util.List;

import com.clokey.server.domain.cloth.domain.entity.Cloth;
import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.history.domain.entity.HistoryCloth;

public interface HistoryClothRepositoryService {

    void save(History history, Cloth cloth);

    void delete(History history, Cloth cloth);

    List<Long> findClothIdsByHistoryId(Long historyId);

    void deleteAllByClothId(Long clothId);

    List<Cloth> findAllClothByHistoryId(Long historyId);

    void deleteAllByHistoryId(Long historyId);

    void saveAll(List<HistoryCloth> historyCloths);

    void deleteAllByHistoryIds(List<Long> historyIds);

    String findMostWornCategory(Long memberId);
}
