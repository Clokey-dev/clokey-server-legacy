package com.clokey.server.domain.report.dto;

import com.clokey.server.domain.history.exception.annotation.*;
import com.clokey.server.domain.member.exception.annotation.IdExist;
import com.clokey.server.domain.member.exception.annotation.IdValid;
import com.clokey.server.domain.model.entity.enums.CommentReportType;
import com.clokey.server.domain.model.entity.enums.HistoryReportType;
import com.clokey.server.domain.model.entity.enums.ProfileReportType;
import com.clokey.server.domain.report.exception.annotation.ReportLength;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ReportRequestDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryReportRequest {

        @HistoryExist
        private Long historyId;

        private HistoryReportType historyReportType;

        @ReportLength
        private String content;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentReportRequest {

        @CommentExist
        private Long commentId;

        private CommentReportType commentReportType;

        @ReportLength
        private String content;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileReportRequest {

        @IdValid
        private String clokeyId;

        private ProfileReportType profileReportType;

        @ReportLength
        private String content;
    }
}
