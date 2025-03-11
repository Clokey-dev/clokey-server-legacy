package com.clokey.server.domain.report.converter;

import com.clokey.server.domain.model.entity.enums.CommentReportType;
import com.clokey.server.domain.model.entity.enums.HistoryReportType;
import com.clokey.server.domain.model.entity.enums.ProfileReportType;
import com.clokey.server.domain.report.domain.entity.CommentReport;
import com.clokey.server.domain.report.domain.entity.HistoryReport;
import com.clokey.server.domain.report.domain.entity.ProfileReport;
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

    public static ReportResponseDTO.AdminReportViewResults toAdminProfileReportViewResults(List<ProfileReport> profileReports){
        return ReportResponseDTO.AdminReportViewResults.builder()
                .results(profileReports.stream()
                        .map(profileReport -> {
                            return ReportResponseDTO.AdminReportViewResult.builder()
                                    .id(profileReport.getId())
                                    .reportedInstanceId(profileReport.getReported().getId())
                                    .reporterClokeyId(profileReport.getReporter().getClokeyId())
                                    .reportStatus(profileReport.getReportStatus())
                                    .content(profileReport.getContent())
                                    .reportTypeResult(toProfileReportTypeResult(profileReport.getProfileReportType()))
                                    .build();
                        })
                        .collect(Collectors.toList()))
                .build();
    }

    private static ReportResponseDTO.ReportTypeResult toProfileReportTypeResult(ProfileReportType profileReportType) {
        return  ReportResponseDTO.ReportTypeResult.builder()
                        .reportType(profileReportType.name())
                        .Title(profileReportType.getTitle())
                        .reportContents(profileReportType.getContents())
                        .build();
    }

    public static ReportResponseDTO.AdminReportViewResults toAdminHistoryReportViewResults(List<HistoryReport> historyReports){
        return ReportResponseDTO.AdminReportViewResults.builder()
                .results(historyReports.stream()
                        .map(historyReport -> {
                            return ReportResponseDTO.AdminReportViewResult.builder()
                                    .id(historyReport.getId())
                                    .reportedInstanceId(historyReport.getHistory().getId())
                                    .reporterClokeyId(historyReport.getMember().getClokeyId())
                                    .reportStatus(historyReport.getReportStatus())
                                    .content(historyReport.getContent())
                                    .reportTypeResult(toHistoryReportTypeResult(historyReport.getHistoryReportType()))
                                    .build();
                        })
                        .collect(Collectors.toList()))
                .build();
    }

    private static ReportResponseDTO.ReportTypeResult toHistoryReportTypeResult(HistoryReportType historyReportType) {
        return  ReportResponseDTO.ReportTypeResult.builder()
                .reportType(historyReportType.name())
                .Title(historyReportType.getTitle())
                .reportContents(historyReportType.getContents())
                .build();
    }

    public static ReportResponseDTO.AdminReportViewResults toAdminCommentReportViewResults(List<CommentReport> commentReports){
        return ReportResponseDTO.AdminReportViewResults.builder()
                .results(commentReports.stream()
                        .map(commentReport -> {
                            return ReportResponseDTO.AdminReportViewResult.builder()
                                    .id(commentReport.getId())
                                    .reportedInstanceId(commentReport.getComment().getId())
                                    .reporterClokeyId(commentReport.getMember().getClokeyId())
                                    .reportStatus(commentReport.getReportStatus())
                                    .content(commentReport.getContent())
                                    .reportTypeResult(toCommentReportTypeResult(commentReport.getCommentReportType()))
                                    .build();
                        })
                        .collect(Collectors.toList()))
                .build();
    }

    private static ReportResponseDTO.ReportTypeResult toCommentReportTypeResult(CommentReportType commentReportType) {
        return  ReportResponseDTO.ReportTypeResult.builder()
                .reportType(commentReportType.name())
                .Title(commentReportType.getTitle())
                .reportContents(commentReportType.getContents())
                .build();
    }
}
