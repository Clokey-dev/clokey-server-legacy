package com.clokey.server.domain.report.application;

import com.clokey.server.domain.model.entity.enums.ReportStatus;
import com.clokey.server.domain.model.entity.enums.ReportType;
import com.clokey.server.domain.report.dto.ReportRequestDTO;
import com.clokey.server.domain.report.dto.ReportResponseDTO;
import org.springframework.web.bind.annotation.RequestParam;

public interface ReportService {

    ReportResponseDTO.HistoryReportInfoResult getHistoryReportInfo(Long historyId);

    ReportResponseDTO.HistoryReportResult getHistoryReportResult(ReportRequestDTO.HistoryReportRequest historyReportRequest, Long memberId);

    ReportResponseDTO.CommentReportInfoResult getCommentReportInfo(Long commentId);

    ReportResponseDTO.CommentReportResult getCommentReportResult(ReportRequestDTO.CommentReportRequest commentReportRequest, Long memberId);

    ReportResponseDTO.ProfileReportInfoResult getProfileReportInfo(String clokeyId);

    ReportResponseDTO.ProfileReportResult getProfileReportResult(ReportRequestDTO.ProfileReportRequest profileReportRequest, Long memberId);

    ReportResponseDTO.AdminReportViewResults getAdminReportViewResults(ReportStatus reportStatus,ReportType reportType, Long reporterId, Long reportedInstanceId);
}
