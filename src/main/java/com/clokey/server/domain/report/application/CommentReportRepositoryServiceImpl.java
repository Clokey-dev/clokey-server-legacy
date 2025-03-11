package com.clokey.server.domain.report.application;

import com.clokey.server.domain.history.domain.repository.CommentRepository;
import com.clokey.server.domain.report.domain.entity.CommentReport;
import com.clokey.server.domain.report.domain.repository.CommentReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentReportRepositoryServiceImpl implements CommentReportRepositoryService {

    private final CommentReportRepository commentReportRepository;

    @Override
    public Long save(CommentReport commentReport) {
        return commentReportRepository.save(commentReport).getId();
    }

    @Override
    public Boolean existsById(Long commentId) {
        return commentReportRepository.existsById(commentId);
    }
}
