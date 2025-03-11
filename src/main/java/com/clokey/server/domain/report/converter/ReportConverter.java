package com.clokey.server.domain.report.converter;

import com.clokey.server.domain.model.entity.enums.CommentReportType;
import com.clokey.server.domain.model.entity.enums.HistoryReportType;
import com.clokey.server.domain.model.entity.enums.ProfileReportType;
import com.clokey.server.domain.report.dto.ReportResponseDTO;

import java.util.List;
import java.util.stream.Collectors;

public class ReportConverter {

    private static List<ReportResponseDTO.ReportTypeResult> convertToHistoryReportTypesList() {
        return HistoryReportType.getAllReportTypes().stream()
                .map(reportMap -> ReportResponseDTO.ReportTypeResult.builder()
                        .reportType((String) reportMap.get("name"))  // Enum name
                        .Title((String) reportMap.get("title"))      // 제목
                        .reportContents((List<String>) reportMap.get("contents")) // 상세 내용 리스트
                        .build()
                )
                .collect(Collectors.toList());
    }

    private static List<ReportResponseDTO.ReportTypeResult> convertToCommentReportTypesList() {
        return CommentReportType.getAllReportTypes().stream()
                .map(reportMap -> ReportResponseDTO.ReportTypeResult.builder()
                        .reportType((String) reportMap.get("name"))  // Enum name
                        .Title((String) reportMap.get("title"))      // 제목
                        .reportContents((List<String>) reportMap.get("contents")) // 상세 내용 리스트
                        .build()
                )
                .collect(Collectors.toList());
    }

    private static List<ReportResponseDTO.ReportTypeResult> convertToProfileReportTypesList() {
        return ProfileReportType.getAllReportTypes().stream()
                .map(reportMap -> ReportResponseDTO.ReportTypeResult.builder()
                        .reportType((String) reportMap.get("name"))  // Enum name
                        .Title((String) reportMap.get("title"))      // 제목
                        .reportContents((List<String>) reportMap.get("contents")) // 상세 내용 리스트
                        .build()
                )
                .collect(Collectors.toList());
    }

    public static ReportResponseDTO.CommentReportInfoResult getCommentReportInfoResult(String clokeyId, String nickName, String userProfile, String commentContent) {
        return ReportResponseDTO.CommentReportInfoResult.builder()
                .clokeyId(clokeyId)
                .nickName(nickName)
                .userProfile(userProfile)
                .commentContent(commentContent)
                .ReportTypeResults(convertToCommentReportTypesList())
                .build();
    }


    public static ReportResponseDTO.HistoryReportInfoResult getHistoryReportInfoResult(String clokeyId, String nickName, String userProfile, String historyContent) {
        return ReportResponseDTO.HistoryReportInfoResult.builder()
                .clokeyId(clokeyId)
                .nickName(nickName)
                .userProfile(userProfile)
                .historyContent(historyContent)
                .ReportTypeResults(convertToHistoryReportTypesList())
                .build();
    }

    public static ReportResponseDTO.ProfileReportInfoResult getProfileReportInfoResult(String clokeyId, String nickName, String userProfile) {
        return ReportResponseDTO.ProfileReportInfoResult.builder()
                .clokeyId(clokeyId)
                .nickName(nickName)
                .userProfile(userProfile)
                .ReportTypeResults(convertToProfileReportTypesList())
                .build();
    }

    public static ReportResponseDTO.HistoryReportResult tohistoryReportResult(Long historyReportId){
        return ReportResponseDTO.HistoryReportResult.builder()
                .historyReportId(historyReportId)
                .build();
    }

    public static ReportResponseDTO.CommentReportResult toCommentReportResult(Long commentReportId){
        return ReportResponseDTO.CommentReportResult.builder()
                .commentReportId(commentReportId)
                .build();
    }

    public static ReportResponseDTO.ProfileReportResult toProfileReportResult(Long profileReportId){
        return ReportResponseDTO.ProfileReportResult.builder()
                .profileReportId(profileReportId)
                .build();
    }
}
