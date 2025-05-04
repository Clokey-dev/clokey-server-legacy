package com.clokey.server.domain.history.application;

import com.clokey.server.domain.history.dto.projection.DailyHistoryClothProjectionDTO;
import com.clokey.server.domain.history.dto.projection.DailyHistoryProjectionDTO;
import com.clokey.server.domain.history.dto.projection.HistoryAccessCheckProjectionDTO;
import com.clokey.server.domain.history.dto.projection.MonthlyHistoryProjectionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.model.entity.enums.Visibility;

public interface HistoryRepositoryService {

    List<MonthlyHistoryProjectionDTO> findHistoriesByMemberAndYearMonth(Long memberId, String yearMonth);

    List<History> findHistoriesByMemberWithinMonth(Long memberId);

    void incrementLikes(Long historyId);

    void decrementLikes(Long historyId);

    History findById(Long historyId);

    HistoryAccessCheckProjectionDTO findAccessInfoByHistoryId(Long historyId);

    DailyHistoryProjectionDTO getDailyHistoryProjectionDTO(Long historyId);

    boolean existsById(Long historyId);

    History save(History history);

    boolean checkHistoryExistOfDate(LocalDate date, Long memberId);

    History getHistoryOfDate(LocalDate date, Long memberId);

    void deleteById(Long historyId);

    List<Boolean> existsByHistoryDateAndMemberIds(LocalDate historyDate, List<Long> memberIds);

    void deleteByHistoryIds(List<Long> historyIds);

    Page<History> findByMemberInAndVisibilityOrderByHistoryDateDesc(List<Member> members, Visibility visibility, Pageable pageable);

    List<History> findTop6ByMemberInAndVisibilityOrderByHistoryDateDesc(List<Member> member, Visibility visibility);

    List<History> findAll();

    Long countHistoryByMember(Member member);

    List<History> findHistoriesByMemberIds(List<Long> memberIds);

    List<History> findHistoriesByMemberIdsAndDateRange(List<Long> memberIds, LocalDate from, LocalDate to);

    Page<History> findHistoriesByMemberIdAndMemberLike(Long memberId, Pageable pageable);
}
