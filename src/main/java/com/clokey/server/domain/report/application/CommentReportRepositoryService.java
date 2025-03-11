package com.clokey.server.domain.report.application;

import com.clokey.server.domain.report.domain.entity.CommentReport;

public interface CommentReportRepositoryService {

    Long save(CommentReport commentReport);

    Boolean existsById(Long commentId);
}
