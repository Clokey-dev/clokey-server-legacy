package com.clokey.server.domain.report.application;

import com.clokey.server.domain.history.application.CommentRepositoryService;
import com.clokey.server.domain.history.application.HistoryRepositoryService;
import com.clokey.server.domain.history.domain.entity.Comment;
import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.member.application.MemberRepositoryService;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.model.entity.enums.ReportStatus;
import com.clokey.server.domain.report.converter.ReportConverter;
import com.clokey.server.domain.report.domain.entity.HistoryReport;
import com.clokey.server.domain.report.dto.ReportRequestDTO;
import com.clokey.server.domain.report.dto.ReportResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService{

    private final HistoryReportRepositoryService historyReportRepositoryService;
    private final HistoryRepositoryService historyRepositoryService;
    private final MemberRepositoryService memberRepositoryService;
    private final CommentRepositoryService commentRepositoryService;

    @Override
    @Transactional(readOnly = true)
    public ReportResponseDTO.getHistoryReportInfoResult getHistoryReportInfo(Long historyId) {
        History history = historyRepositoryService.findById(historyId);
        Member historyWriter = history.getMember();
        return ReportConverter.getHistoryReportInfoResult(historyWriter.getClokeyId(),
                historyWriter.getNickname(),
                historyWriter.getProfileImageUrl(),
                history.getContent());
    }

    @Override
    @Transactional
    public ReportResponseDTO.historyReportResult getHistoryReportResult(ReportRequestDTO.HistoryReportRequest historyReportRequest, Long memberId) {
        History reportedHistory = historyRepositoryService.findById(historyReportRequest.getHistoryId());

        HistoryReport historyReport = HistoryReport.builder()
                .historyReportType(historyReportRequest.getHistoryReportType())
                .reportStatus(ReportStatus.UNCHECKED)
                .content(historyReportRequest.getContent())
                .history(reportedHistory)
                .member(memberRepositoryService.findMemberById(memberId))
                .build();

        Long id = historyReportRepositoryService.save(historyReport);

        return ReportConverter.historyReportResult(id);
    }

    @Override
    public ReportResponseDTO.getCommentReportInfoResult getCommentReportInfo(Long commentId) {
        Comment comment = commentRepositoryService.findById(commentId);
        Member commentWriter = comment.getMember();
        return ReportConverter.getCommentReportInfoResult(commentWriter.getClokeyId(),
                commentWriter.getNickname(),
                commentWriter.getProfileImageUrl(),
                comment.getContent());
    }


}
