package com.clokey.server.domain.report.application;

import com.clokey.server.domain.report.domain.entity.HistoryReport;
import com.querydsl.core.types.Predicate;

import java.util.List;

public interface HistoryReportRepositoryService {

    Long save(HistoryReport historyReport);

    List<HistoryReport> findAllByPredicate(Predicate predicate);
}
