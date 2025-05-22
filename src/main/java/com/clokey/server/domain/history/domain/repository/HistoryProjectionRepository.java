package com.clokey.server.domain.history.domain.repository;

import com.clokey.server.domain.history.dto.projection.DailyHistoryClothProjectionDTO;
import com.clokey.server.domain.history.dto.projection.HistoryProjectionDTO;

import java.util.List;

public interface HistoryProjectionRepository {

    List<HistoryProjectionDTO> getHistoriesByMemberAndYearMonth(Long memberId, String yearMonth);

    HistoryProjectionDTO getDailyHistory(Long historyId);

    List<DailyHistoryClothProjectionDTO> findClothesByHistoryId(Long historyId);
}
