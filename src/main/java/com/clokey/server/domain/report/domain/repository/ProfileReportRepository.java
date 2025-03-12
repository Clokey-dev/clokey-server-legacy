package com.clokey.server.domain.report.domain.repository;

import com.clokey.server.domain.report.domain.entity.CommentReport;
import com.querydsl.core.types.Predicate;
import com.clokey.server.domain.report.domain.entity.ProfileReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface ProfileReportRepository extends JpaRepository<ProfileReport, Long> , QuerydslPredicateExecutor<ProfileReport> {

    List<ProfileReport> findAll(Predicate predicate);
}
