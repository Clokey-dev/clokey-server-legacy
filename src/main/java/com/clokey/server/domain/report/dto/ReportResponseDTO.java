package com.clokey.server.domain.report.dto;


import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.model.entity.BaseEntity;
import com.clokey.server.domain.model.entity.enums.HistoryReportType;
import com.clokey.server.domain.model.entity.enums.ProfileReportType;
import com.clokey.server.domain.model.entity.enums.ReportStatus;
import jakarta.persistence.*;
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
    public static class ProfileReportInfoResult {
        private String clokeyId;
        private String nickName;
        private String userProfile;
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

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileReportResult {
        private Long profileReportId;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminReportViewResults {
        private List<AdminReportViewResult> results;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminReportViewResult {
        private Long id;
        private String reporterClokeyId;
        private Long reportedInstanceId;
        private ReportTypeResult reportTypeResult;
        private String content;
        private ReportStatus reportStatus;
    }



}

