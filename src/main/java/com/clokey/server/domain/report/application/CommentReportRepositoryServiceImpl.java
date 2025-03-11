package com.clokey.server.domain.report.application;

import com.clokey.server.domain.history.domain.repository.CommentRepository;
import com.clokey.server.domain.report.domain.entity.CommentReport;
import com.clokey.server.domain.report.domain.repository.CommentReportRepository;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public List<CommentReport> findAllByPredicate(Predicate predicate) {
        return commentReportRepository.findAll(predicate);
    }
}
