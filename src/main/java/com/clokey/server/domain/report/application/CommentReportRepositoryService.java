package com.clokey.server.domain.report.application;

import com.clokey.server.domain.report.domain.entity.CommentReport;
import com.querydsl.core.types.Predicate;

import java.util.List;

public interface CommentReportRepositoryService {

    Long save(CommentReport commentReport);

    Boolean existsById(Long commentId);

    List<CommentReport> findAllByPredicate(Predicate predicate);

    CommentReport findById(Long id);
}
