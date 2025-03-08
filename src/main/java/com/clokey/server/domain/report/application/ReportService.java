package com.clokey.server.domain.report.application;

import com.clokey.server.domain.report.dto.ReportRequestDTO;
import com.clokey.server.domain.report.dto.ReportResponseDTO;

public interface ReportService {

    ReportResponseDTO.HistoryReportInfoResult getHistoryReportInfo(Long historyId);

    ReportResponseDTO.HistoryReportResult getHistoryReportResult(ReportRequestDTO.HistoryReportRequest historyReportRequest, Long memberId);

    ReportResponseDTO.CommentReportInfoResult getCommentReportInfo(Long commentId);

    ReportResponseDTO.CommentReportResult getCommentReportResult(ReportRequestDTO.CommentReportRequest commentReportRequest, Long memberId);

    ReportResponseDTO.ProfileReportInfoResult getProfileReportInfo(String clokeyId);

    ReportResponseDTO.ProfileReportResult getProfileReportResult(ReportRequestDTO.ProfileReportRequest profileReportRequest, Long memberId);
}
