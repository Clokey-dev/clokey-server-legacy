package com.clokey.server.domain.report.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class ReportResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryReportInfoResult {
        private String clokeyId;
        private String nickName;
        private String userProfile;
        private String  historyContent;
        private List<ReportTypeResult> ReportTypeResults;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentReportInfoResult {
        private String clokeyId;
        private String nickName;
        private String userProfile;
        private String  commentContent;
        private List<ReportTypeResult> ReportTypeResults;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportTypeResult {
        private String reportType;
        private String Title;
        private List<String> reportContents;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryReportResult {
        private Long historyReportId;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentReportResult {
        private Long commentReportId;
    }

}
