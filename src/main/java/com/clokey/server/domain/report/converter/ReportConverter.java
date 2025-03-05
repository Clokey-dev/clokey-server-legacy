package com.clokey.server.domain.report.converter;

import com.clokey.server.domain.history.dto.HistoryResponseDTO;
import com.clokey.server.domain.model.entity.enums.CommentReportType;
import com.clokey.server.domain.model.entity.enums.HistoryReportType;
import com.clokey.server.domain.report.dto.ReportRequestDTO;
import com.clokey.server.domain.report.dto.ReportResponseDTO;

import java.util.List;
import java.util.stream.Collectors;

public class ReportConverter {

    private static List<ReportResponseDTO.reportType> convertToHistoryReportTypesList() {
        return HistoryReportType.getAllReportTypes().stream()
                .map(reportMap -> ReportResponseDTO.reportType.builder()
                        .reportType((String) reportMap.get("name"))  // Enum name
                        .Title((String) reportMap.get("title"))      // 제목
                        .reportContents((List<String>) reportMap.get("contents")) // 상세 내용 리스트
                        .build()
                )
                .collect(Collectors.toList());
    }

    private static List<ReportResponseDTO.reportType> convertToCommentReportTypesList() {
        return CommentReportType.getAllReportTypes().stream()
                .map(reportMap -> ReportResponseDTO.reportType.builder()
                        .reportType((String) reportMap.get("name"))  // Enum name
                        .Title((String) reportMap.get("title"))      // 제목
                        .reportContents((List<String>) reportMap.get("contents")) // 상세 내용 리스트
                        .build()
                )
                .collect(Collectors.toList());
    }

    public static ReportResponseDTO.getCommentReportInfoResult getCommentReportInfoResult(String clokeyId, String nickName, String userProfile, String commentContent) {
        return ReportResponseDTO.getCommentReportInfoResult.builder()
                .clokeyId(clokeyId)
                .nickName(nickName)
                .userProfile(userProfile)
                .commentContent(commentContent)
                .reportTypes(convertToCommentReportTypesList())
                .build();
    }


    public static ReportResponseDTO.getHistoryReportInfoResult getHistoryReportInfoResult(String clokeyId, String nickName, String userProfile, String historyContent) {
        return ReportResponseDTO.getHistoryReportInfoResult.builder()
                .clokeyId(clokeyId)
                .nickName(nickName)
                .userProfile(userProfile)
                .historyContent(historyContent)
                .reportTypes(convertToHistoryReportTypesList())
                .build();
    }

    public static ReportResponseDTO.historyReportResult historyReportResult(Long historyReportId){
        return ReportResponseDTO.historyReportResult.builder()
                .historyReportId(historyReportId)
                .build();
    }
}
