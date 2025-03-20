package com.clokey.server.domain.report.application;

import com.clokey.server.domain.report.domain.entity.HistoryReport;
import com.clokey.server.domain.report.domain.repository.HistoryReportRepository;
import com.clokey.server.domain.report.exception.ReportException;
import com.clokey.server.global.error.code.status.ErrorStatus;
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

    @Override
    public HistoryReport findById(Long id) {
        return historyReportRepository.findById(id).orElseThrow(()-> new ReportException(ErrorStatus.NO_SUCH_HISTORY_REPORT));
    }
}
