package com.clokey.server.domain.report.domain.repository;

import com.clokey.server.domain.report.domain.entity.CommentReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import com.querydsl.core.types.Predicate;

public interface CommentReportRepository extends JpaRepository<CommentReport, Long> {

    List<CommentReport> findAll(Predicate predicate);
}
