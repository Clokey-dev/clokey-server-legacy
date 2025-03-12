package com.clokey.server.domain.report.application;

import com.clokey.server.domain.report.domain.entity.HistoryReport;
import com.clokey.server.domain.report.domain.repository.HistoryReportRepository;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryReportRepositoryServiceImpl implements HistoryReportRepositoryService {

    private final HistoryReportRepository historyReportRepository;

    @Override
    public Long save(HistoryReport historyReport) {
        return historyReportRepository.save(historyReport).getId();
    }

    @Override
    public List<HistoryReport> findAllByPredicate(Predicate predicate) {
        return historyReportRepository.findAll(predicate);
    }
}
