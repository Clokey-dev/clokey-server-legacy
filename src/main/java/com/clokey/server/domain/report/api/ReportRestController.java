package com.clokey.server.domain.report.api;

import com.clokey.server.domain.history.exception.annotation.CommentExist;
import com.clokey.server.domain.history.exception.annotation.HistoryExist;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.member.exception.annotation.AuthUser;
import com.clokey.server.domain.report.application.ReportService;
import com.clokey.server.domain.report.dto.ReportRequestDTO;
import com.clokey.server.domain.report.dto.ReportResponseDTO;
import com.clokey.server.global.common.response.BaseResponse;
import com.clokey.server.global.error.code.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/report")
@Validated
public class ReportRestController {

    private final ReportService reportService;

    @GetMapping("/history")
    @Operation(summary = "기록 신고 정보 조회 API", description = "기록 신고 관련 정보를 조회할 수 있는 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "REPORT_200", description = "기록 신고의 정보가 성공적으로 조회되었습니다."),
    })
    public BaseResponse<ReportResponseDTO.HistoryReportInfoResult> getHistoryReportInformation(@RequestParam(value = "historyId") @HistoryExist Long historyId) {

        ReportResponseDTO.HistoryReportInfoResult result = reportService.getHistoryReportInfo(historyId);

        return BaseResponse.onSuccess(SuccessStatus.REPORT_HISTORY_VIEW_SUCCESS, result);
    }

    @PostMapping("/history")
    @Operation(summary = "기록 신고 API", description = "기록을 신고할 수 있는 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "REPORT_201", description = "기록을 성공적으로 신고했습니다."),
    })
    public BaseResponse<ReportResponseDTO.HistoryReportResult> reportHistory(@Parameter(name = "user", hidden = true) @AuthUser Member member,
                                                                             @RequestBody @Valid ReportRequestDTO.HistoryReportRequest historyReportRequest) {

        ReportResponseDTO.HistoryReportResult result = reportService.getHistoryReportResult(historyReportRequest,member.getId());

        return BaseResponse.onSuccess(SuccessStatus.REPORT_HISTORY_SUCCESS, result);
    }

    @GetMapping("/comment")
    @Operation(summary = "댓글 신고 정보 조회 API", description = "댓글 신고 관련 정보를 조회할 수 있는 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "REPORT_200", description = "댓글 신고의 정보가 성공적으로 조회되었습니다."),
    })
    public BaseResponse<ReportResponseDTO.CommentReportInfoResult> getCommentReportInformation(@RequestParam(value = "commentId") @CommentExist Long commentId) {

        ReportResponseDTO.CommentReportInfoResult result = reportService.getCommentReportInfo(commentId);

        return BaseResponse.onSuccess(SuccessStatus.REPORT_COMMENT_VIEW_SUCCESS, result);
    }

    @PostMapping("/comment")
    @Operation(summary = "댓글 신고 API", description = "댓글을 신고할 수 있는 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "REPORT_201", description = "댓글을 성공적으로 신고했습니다."),
    })
    public BaseResponse<ReportResponseDTO.CommentReportResult> reportComment(@Parameter(name = "user", hidden = true) @AuthUser Member member,
                                                                             @RequestBody @Valid ReportRequestDTO.CommentReportRequest commentReportRequest) {

        ReportResponseDTO.CommentReportResult result = reportService.getCommentReportResult(commentReportRequest, member.getId());

        return BaseResponse.onSuccess(SuccessStatus.REPORT_COMMENT_SUCCESS, result);
    }
}
