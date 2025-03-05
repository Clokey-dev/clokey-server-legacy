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
    public static class getHistoryReportInfoResult {
        private String clokeyId;
        private String nickName;
        private String userProfile;
        private String  historyContent;
        private List<reportType> reportTypes;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class reportType {
        private String reportType;
        private String Title;
        private List<String> reportContents;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class historyReportResult {
        private Long historyReportId;
    }


}
