package com.clokey.server.domain.report.application;

import com.clokey.server.domain.report.dto.ReportRequestDTO;
import com.clokey.server.domain.report.dto.ReportResponseDTO;

public interface ReportService {

    ReportResponseDTO.getHistoryReportInfoResult getHistoryReportInfo(Long historyId);

    ReportResponseDTO.historyReportResult getHistoryReportResult(ReportRequestDTO.HistoryReportRequest historyReportRequest, Long memberId);

    ReportResponseDTO.getCommentReportInfoResult getCommentReportInfo(Long commentId);


}
