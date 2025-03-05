package com.clokey.server.domain.report.application;

import com.clokey.server.domain.history.application.HistoryRepositoryService;
import com.clokey.server.domain.report.domain.entity.HistoryReport;
import com.clokey.server.domain.report.domain.repository.HistoryReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HistoryRepositoryServiceImpl implements HistoryReportRepositoryService {

    HistoryReportRepository historyReportRepository;

    @Override
    public Long save(HistoryReport historyReport) {
        return historyReportRepository.save(historyReport).getId();
    }
}
