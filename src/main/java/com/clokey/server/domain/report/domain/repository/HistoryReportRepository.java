package com.clokey.server.domain.report.domain.repository;

import com.clokey.server.domain.history.domain.entity.Comment;
import com.clokey.server.domain.report.domain.entity.HistoryReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryReportRepository extends JpaRepository<HistoryReport, Long> {
}
