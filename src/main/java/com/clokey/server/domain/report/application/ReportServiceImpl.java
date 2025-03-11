package com.clokey.server.domain.report.application;

import com.clokey.server.domain.history.application.CommentRepositoryService;
import com.clokey.server.domain.history.application.HistoryRepositoryService;
import com.clokey.server.domain.history.domain.entity.Comment;
import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.member.application.MemberRepositoryService;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.model.entity.enums.ReportType;
import com.clokey.server.domain.report.domain.entity.*;
import com.clokey.server.domain.model.entity.enums.ReportStatus;
import com.clokey.server.domain.report.converter.ReportConverter;
import com.clokey.server.domain.report.dto.ReportRequestDTO;
import com.clokey.server.domain.report.dto.ReportResponseDTO;
import com.clokey.server.domain.report.exception.ReportException;
import com.clokey.server.global.error.code.status.ErrorStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService{

    private final HistoryReportRepositoryService historyReportRepositoryService;
    private final HistoryRepositoryService historyRepositoryService;
    private final MemberRepositoryService memberRepositoryService;
    private final CommentRepositoryService commentRepositoryService;
    private final CommentReportRepositoryService commentReportRepositoryService;
    private final ProfileReportRepositoryService profileReportRepositoryService;

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





    @Override
    @Transactional(readOnly = true)
    public ReportResponseDTO.ProfileReportInfoResult getProfileReportInfo(String clokeyId) {
        Member reported = memberRepositoryService.findMemberByClokeyId(clokeyId);
        return ReportConverter.getProfileReportInfoResult(clokeyId,
                reported.getNickname(),
                reported.getProfileImageUrl());
    }


    @Override
    @Transactional
    public ReportResponseDTO.ProfileReportResult getProfileReportResult(ReportRequestDTO.ProfileReportRequest profileReportRequest, Long memberId) {
        Member reporter = memberRepositoryService.findMemberById(memberId);
        Member reported = memberRepositoryService.findMemberByClokeyId(profileReportRequest.getClokeyId());

        ProfileReport profileReport = ProfileReport.builder()
                .profileReportType(profileReportRequest.getProfileReportType())
                .reportStatus(ReportStatus.UNCHECKED)
                .content(profileReportRequest.getContent())
                .reporter(reporter)
                .reported(reported)
                .build();

        Long profileReportId = profileReportRepositoryService.save(profileReport);

        return ReportConverter.toProfileReportResult(profileReportId);
    }

    @Override
    public ReportResponseDTO.AdminReportViewResults getAdminReportViewResults(ReportStatus reportStatus, ReportType reportType, Long reporterId, Long reportedInstanceId) {

        validateReportTypeAndReportedInstanceId(reportType,reportedInstanceId);

        BooleanBuilder builder = new BooleanBuilder();

        if (reportType == null) {
            return getAllReports(reportStatus, reporterId, reportedInstanceId);
        }

        // reportType이 지정되었을 경우, 해당 타입에 맞는 필터링
        switch (reportType) {
            case PROFILE:
                return getFilteredReportsForProfile(builder, reportStatus, reporterId, reportedInstanceId);

            case HISTORY:
                return getFilteredReportsForHistory(builder, reportStatus, reporterId, reportedInstanceId);

            case COMMENT:
                return getFilteredReportsForComment(builder, reportStatus, reporterId, reportedInstanceId);

            default:
                throw new ReportException(ErrorStatus.INVALID_REPORT_TYPE);
        }
    }
    private ReportResponseDTO.AdminReportViewResults getFilteredReportsForProfile(BooleanBuilder builder,
                                                                                  ReportStatus reportStatus,
                                                                                  Long reporterId,
                                                                                  Long reportedInstanceId) {
        if (reportStatus != null) {
            builder.and(QProfileReport.profileReport.reportStatus.eq(reportStatus));
        }
        if (reporterId != null) {
            builder.and(QProfileReport.profileReport.reporter.id.eq(reporterId));
        }
        if (reportedInstanceId != null) {
            builder.and(QProfileReport.profileReport.reported.id.eq(reportedInstanceId));
        }

        return getReportResponse(builder, ReportType.PROFILE);
    }

    private ReportResponseDTO.AdminReportViewResults getFilteredReportsForHistory(BooleanBuilder builder,
                                                                                  ReportStatus reportStatus,
                                                                                  Long reporterId,
                                                                                  Long reportedInstanceId) {
        if (reportStatus != null) {
            builder.and(QHistoryReport.historyReport.reportStatus.eq(reportStatus));
        }
        if (reporterId != null) {
            builder.and(QHistoryReport.historyReport.member.id.eq(reporterId));
        }
        if (reportedInstanceId != null) {
            builder.and(QHistoryReport.historyReport.history.id.eq(reportedInstanceId));
        }

        return getReportResponse(builder, ReportType.HISTORY);
    }

    private ReportResponseDTO.AdminReportViewResults getFilteredReportsForComment(BooleanBuilder builder,
                                                                                  ReportStatus reportStatus,
                                                                                  Long reporterId,
                                                                                  Long reportedInstanceId) {
        if (reportStatus != null) {
            builder.and(QCommentReport.commentReport.reportStatus.eq(reportStatus));
        }
        if (reporterId != null) {
            builder.and(QCommentReport.commentReport.member.id.eq(reporterId));
        }
        if (reportedInstanceId != null) {
            builder.and(QCommentReport.commentReport.comment.id.eq(reportedInstanceId));
        }
        return getReportResponse(builder, ReportType.COMMENT);
    }

    private ReportResponseDTO.AdminReportViewResults getAllReports(ReportStatus reportStatus,
                                                                   Long reporterId,
                                                                   Long reportedInstanceId) {

        BooleanBuilder profilePredicate = new BooleanBuilder();
        BooleanBuilder historyPredicate = new BooleanBuilder();
        BooleanBuilder commentPredicate = new BooleanBuilder();

        if (reportStatus != null) {
            profilePredicate.and(QProfileReport.profileReport.reportStatus.eq(reportStatus));
            historyPredicate.and(QHistoryReport.historyReport.reportStatus.eq(reportStatus));
            commentPredicate.and(QCommentReport.commentReport.reportStatus.eq(reportStatus));
        }
        if (reporterId != null) {
            Member reporter = memberRepositoryService.findMemberById(reporterId);
            profilePredicate.and(QProfileReport.profileReport.reporter.eq(reporter));
            historyPredicate.and(QHistoryReport.historyReport.member.eq(reporter));
            commentPredicate.and(QCommentReport.commentReport.member.eq(reporter));
        }
        if (reportedInstanceId != null) {
            profilePredicate.and(QProfileReport.profileReport.reported.eq(memberRepositoryService.findMemberById(reportedInstanceId)));
            historyPredicate.and(QHistoryReport.historyReport.history.eq(historyRepositoryService.findById(reportedInstanceId)));
            commentPredicate.and(QCommentReport.commentReport.comment.eq(commentRepositoryService.findById(reportedInstanceId)));
        }

        List<ProfileReport> profileReports = profileReportRepositoryService.findAllByPredicate(profilePredicate);
        List<HistoryReport> historyReports = historyReportRepositoryService.findAllByPredicate(historyPredicate);
        List<CommentReport> commentReports = commentReportRepositoryService.findAllByPredicate(commentPredicate);

        // 모든 리포트 데이터 조회
        return ReportConverter.toAllAdminReportViewResults(profileReports,commentReports,historyReports);
    }


    private ReportResponseDTO.AdminReportViewResults getReportResponse(BooleanBuilder builder, ReportType reportType) {

        switch (reportType) {
            case PROFILE:
                List<ProfileReport> profileReports = profileReportRepositoryService.findAllByPredicate(builder);
                return ReportConverter.toAdminProfileReportViewResults(profileReports);
            case HISTORY:
                List<HistoryReport> historyReports = historyReportRepositoryService.findAllByPredicate(builder);
                return ReportConverter.toAdminHistoryReportViewResults(historyReports);
            case COMMENT:
                List<CommentReport> commentReports = commentReportRepositoryService.findAllByPredicate(builder);
                return ReportConverter.toAdminCommentReportViewResults(commentReports);
            default:
                throw new ReportException(ErrorStatus.INVALID_REPORT_TYPE);
        }
    }

        private void validateReportTypeAndReportedInstanceId (ReportType reportType, Long reportedInstanceId){

            //ReportedInstanced 이 null이면 검증의 대상이 아니게 됩니다.
            if (reportedInstanceId == null) {
                return;
            }

            if (reportType == null) {
                throw new ReportException(ErrorStatus.REPORT_INSTANCE_ID_WITHOUT_REPORT_TYPE);
            }

            if (reportType.equals(ReportType.PROFILE) && !memberRepositoryService.memberExist(reportedInstanceId)) {
                throw new ReportException(ErrorStatus.NO_SUCH_REPORT_INSTANCE_ID);
            }

            if (reportType.equals(ReportType.HISTORY) && !historyRepositoryService.existsById(reportedInstanceId)) {
                throw new ReportException(ErrorStatus.NO_SUCH_REPORT_INSTANCE_ID);
            }

            if (reportType.equals(ReportType.COMMENT) && !commentReportRepositoryService.existsById(reportedInstanceId)) {
                throw new ReportException(ErrorStatus.NO_SUCH_REPORT_INSTANCE_ID);
            }
        }
}


