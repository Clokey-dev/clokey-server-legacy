package com.clokey.server.domain.history.domain.repository;

import com.clokey.server.domain.history.dto.projection.DailyHistoryClothProjectionDTO;
import com.clokey.server.domain.history.dto.projection.HistoryProjectionDTO;

import java.util.List;

public interface HistoryProjectionRepository {

    List<HistoryProjectionDTO> getMonthlyHistoriesByMemberAndYearMonth(Long memberId, String yearMonth);

    List<DailyHistoryClothProjectionDTO> findClothesByHistoryId(Long historyId);
}
