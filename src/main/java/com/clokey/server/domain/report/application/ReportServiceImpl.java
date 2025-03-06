package com.clokey.server.domain.report.application;

import com.clokey.server.domain.history.application.CommentRepositoryService;
import com.clokey.server.domain.history.application.HistoryRepositoryService;
import com.clokey.server.domain.history.domain.entity.Comment;
import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.member.application.MemberRepositoryService;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.model.entity.enums.ReportStatus;
import com.clokey.server.domain.report.converter.ReportConverter;
import com.clokey.server.domain.report.domain.entity.CommentReport;
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
    private final CommentReportRepositoryService commentReportRepositoryService;

    @Override
    @Transactional(readOnly = true)
    public ReportResponseDTO.HistoryReportInfoResult getHistoryReportInfo(Long historyId) {
        History history = historyRepositoryService.findById(historyId);
        Member historyWriter = history.getMember();
        return ReportConverter.getHistoryReportInfoResult(historyWriter.getClokeyId(),
                historyWriter.getNickname(),
                historyWriter.getProfileImageUrl(),
                history.getContent());
    }

    @Override
    @Transactional
    public ReportResponseDTO.HistoryReportResult getHistoryReportResult(ReportRequestDTO.HistoryReportRequest historyReportRequest, Long memberId) {
        History reportedHistory = historyRepositoryService.findById(historyReportRequest.getHistoryId());

        HistoryReport historyReport = HistoryReport.builder()
                .historyReportType(historyReportRequest.getHistoryReportType())
                .reportStatus(ReportStatus.UNCHECKED)
                .content(historyReportRequest.getContent())
                .history(reportedHistory)
                .member(memberRepositoryService.findMemberById(memberId))
                .build();

        Long historyReportId = historyReportRepositoryService.save(historyReport);

        return ReportConverter.tohistoryReportResult(historyReportId);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponseDTO.CommentReportInfoResult getCommentReportInfo(Long commentId) {
        Comment comment = commentRepositoryService.findById(commentId);
        Member commentWriter = comment.getMember();
        return ReportConverter.getCommentReportInfoResult(commentWriter.getClokeyId(),
                commentWriter.getNickname(),
                commentWriter.getProfileImageUrl(),
                comment.getContent());
    }

    @Override
    @Transactional
    public ReportResponseDTO.CommentReportResult getCommentReportResult(ReportRequestDTO.CommentReportRequest commentReportRequest, Long memberId) {
        Comment reportedComment = commentRepositoryService.findById(commentReportRequest.getCommentId());

        CommentReport commentReport = CommentReport.builder()
                .commentReportType(commentReportRequest.getCommentReportType())
                .reportStatus(ReportStatus.UNCHECKED)
                .content(commentReportRequest.getContent())
                .comment(reportedComment)
                .member(memberRepositoryService.findMemberById(memberId))
                .build();

        Long commentReportId = commentReportRepositoryService.save(commentReport);
        return ReportConverter.toCommentReportResult(commentReportId);
    }


}
