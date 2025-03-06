package com.clokey.server.domain.report.application;

import com.clokey.server.domain.report.domain.entity.HistoryReport;

public interface HistoryReportRepositoryService {

    Long save(HistoryReport historyReport);
}
