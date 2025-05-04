package com.clokey.server.domain.history.application;

import com.clokey.server.domain.history.dto.projection.MonthlyHistoryProjectionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.history.domain.repository.HistoryRepository;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.model.entity.enums.Visibility;
import com.clokey.server.global.error.code.status.ErrorStatus;
import com.clokey.server.global.error.exception.DatabaseException;

@Service
@RequiredArgsConstructor
public class HistoryRepositoryServiceImpl implements HistoryRepositoryService {

    private final HistoryRepository historyRepository;

    @Override
    public List<MonthlyHistoryProjectionDTO> findHistoriesByMemberAndYearMonth(Long memberId, String yearMonth) {
        return historyRepository.findHistoriesByMemberAndYearMonth(memberId, yearMonth);
    }

    @Override
    public List<History> findHistoriesByMemberWithinMonth(Long memberId) {
        LocalDate monthAgo = LocalDate.now().minusMonths(1);
        return historyRepository.findHistoriesWithinMonth(memberId, monthAgo);
    }

    @Override
    public void incrementLikes(Long historyId) {
        historyRepository.incrementLikes(historyId);
    }

    @Override
    public void decrementLikes(Long historyId) {
        historyRepository.decrementLikes(historyId);
    }

    @Override
    public History findById(Long historyId) {
        return historyRepository.findById(historyId).orElseThrow(() -> new DatabaseException(ErrorStatus.NO_SUCH_HISTORY));
    }

    @Override
    public boolean existsById(Long historyId) {
        return historyRepository.existsById(historyId);
    }

    @Override
    public History save(History history) {
        return historyRepository.save(history);
    }

    @Override
    public boolean checkHistoryExistOfDate(LocalDate date, Long memberId) {
        return historyRepository.existsByHistoryDateAndMember_Id(date, memberId);
    }

    @Override
    public History getHistoryOfDate(LocalDate date, Long memberId) {
        return historyRepository.findByHistoryDateAndMember_Id(date, memberId)
                .orElseThrow(() -> new DatabaseException(ErrorStatus.NO_HISTORY_FOR_DATE));
    }

    @Override
    public void deleteById(Long historyId) {
        historyRepository.deleteById(historyId);
    }

    @Override
    public List<Boolean> existsByHistoryDateAndMemberIds(LocalDate historyDate, List<Long> memberId) {
        return historyRepository.existsByHistoryDateAndMemberIds(historyDate, memberId);
    }

    @Override
    public void deleteByHistoryIds(List<Long> historyIds) {
        historyRepository.deleteByHistoryIds(historyIds);
    }

    public Page<History> findByMemberInAndVisibilityOrderByHistoryDateDesc(List<Member> members, Visibility visibility, Pageable pageable) {
        return historyRepository.findByMemberInAndVisibilityOrderByHistoryDateDesc(members, visibility, pageable);
    }

    @Override
    public List<History> findTop6ByMemberInAndVisibilityOrderByHistoryDateDesc(List<Member> member, Visibility visibility) {
        return historyRepository.findTop6ByMemberInAndVisibilityAndHistoryDateAfterOrderByHistoryDateDesc(member, visibility, LocalDate.now().minusWeeks(2));
    }

    @Override
    public List<History> findAll() {
        return historyRepository.findAll();
    }

    @Override
    public Long countHistoryByMember(Member member) {
        return historyRepository.countHistoryByMember(member);
    }

    @Override
    public List<History> findHistoriesByMemberIds(List<Long> memberIds) {
        return historyRepository.findHistoryByMemberIdIn(memberIds);
    }

    @Override
    public List<History> findHistoriesByMemberIdsAndDateRange(List<Long> memberIds, LocalDate from, LocalDate to) {
        return historyRepository.findHistoriesByMemberIdsAndDateRange(memberIds, from, to);
    }

    @Override
    public Page<History> findHistoriesByMemberIdAndMemberLike(Long memberId, Pageable pageable) {
        return historyRepository.findLikedHistories(memberId, pageable);
    }
}
