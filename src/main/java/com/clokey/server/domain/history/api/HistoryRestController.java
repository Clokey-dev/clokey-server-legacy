package com.clokey.server.domain.history.api;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.history.application.HistoryService;
import com.clokey.server.domain.history.dto.HistoryRequestDTO;
import com.clokey.server.domain.history.dto.HistoryResponseDTO;
import com.clokey.server.domain.history.exception.annotation.HistoryExist;
import com.clokey.server.domain.history.exception.annotation.HistoryImageQuantityLimit;
import com.clokey.server.domain.history.exception.annotation.MonthFormat;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.member.exception.annotation.AuthUser;
import com.clokey.server.domain.member.exception.annotation.NullableClokeyIdExist;
import com.clokey.server.global.common.response.BaseResponse;
import com.clokey.server.global.error.code.status.SuccessStatus;
import com.clokey.server.global.error.exception.annotation.CheckPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequiredArgsConstructor
@RequestMapping("/histories")
@Validated
public class HistoryRestController {

    private final HistoryService historyService;

    @GetMapping("/{historyId}")
    @Operation(summary = "특정 유저의 특정 일의 기록을 확인할 수 있는 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "HISTORY_200", description = "OK, 성공적으로 조회되었습니다."),
    })
    @Parameters({
            @Parameter(name = "historyId", description = "기록의 id, path variable 입니다.")
    })
    public BaseResponse<HistoryResponseDTO.DailyHistoryResult> getDailyHistory(@PathVariable @Valid @HistoryExist Long historyId,
                                                                               @Parameter(name = "user", hidden = true) @AuthUser Member member) {

        HistoryResponseDTO.DailyHistoryResult result = historyService.getDaily(historyId, member.getId());

        return BaseResponse.onSuccess(SuccessStatus.HISTORY_SUCCESS, result);
    }

    @GetMapping("/monthly")
    @Operation(summary = "특정 유저의 특정 월의 기록을 확인할 수 있는 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "HISTORY_200", description = "성공적으로 조회되었습니다."),
    })
    @Parameters({
            @Parameter(name = "clokeyId", description = "조회하고자 하는 clokeyId, 빈칸 입력시 현재 유저를 기준으로 합니다."),
            @Parameter(name = "month", description = "조회하고자 하는 월입니다. YYYY-MM 형식으로 입력해주세요. ex)2025-01")
    })
    public BaseResponse<HistoryResponseDTO.MonthViewResult> getMonthlyHistories(@Parameter(name = "user", hidden = true) @AuthUser Member member,
                                                                                @RequestParam(value = "clokeyId", required = false) @Valid @NullableClokeyIdExist String clokeyId,
                                                                                @RequestParam(value = "month") @Valid @MonthFormat String month) {

        HistoryResponseDTO.MonthViewResult result = historyService.getMonthlyHistories(member.getId(), clokeyId, month);

        return BaseResponse.onSuccess(SuccessStatus.HISTORY_SUCCESS, result);

    }

    @GetMapping("/{historyId}/comments")
    @Operation(summary = "특정 기록의 댓글을 읽어올 수 있는 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "HISTORY_200", description = "OK, 성공적으로 조회되었습니다.")
    })
    @Parameters({
            @Parameter(name = "historyId", description = "기록의 id, path variable 입니다."),
            @Parameter(name = "page", description = "페이징 관련 query parameter")

    })
    public BaseResponse<HistoryResponseDTO.HistoryCommentResult> getComments(@PathVariable @Valid @HistoryExist Long historyId,
                                                                             @RequestParam(value = "page") @Valid @CheckPage int page) {
        //페이지를 1에서 부터 받기 위해서 -1을 해서 입력합니다.
        HistoryResponseDTO.HistoryCommentResult result = historyService.getComments(historyId, page - 1);

        return BaseResponse.onSuccess(SuccessStatus.HISTORY_SUCCESS, result);
    }

    @PostMapping("/like")
    @Operation(summary = "특정 기록에 좋아요를 누를 수 있는 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "HISTORY_200", description = "좋아요 상태가 성공적으로 변경되었습니다."),
    })
    public BaseResponse<HistoryResponseDTO.LikeResult> like(@Parameter(name = "user", hidden = true) @AuthUser Member member,
                                                            @RequestBody @Valid HistoryRequestDTO.LikeStatusChange request) {

        //isLiked의 상태에 따라서 좋아요 -> 취소 , 좋아요가 없는 상태 -> 좋아요 로 바꿔주게 됩니다.
        HistoryResponseDTO.LikeResult result = historyService.changeLike(member.getId(), request.getHistoryId(), request.isLiked());

        return BaseResponse.onSuccess(SuccessStatus.HISTORY_LIKE_STATUS_CHANGED, result);
    }

    @GetMapping("/{historyId}/likes")
    @Operation(summary = "특정 기록에 좋아요를 누른 유저의 정보를 확인합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "HISTORY_200", description = "기록의 좋아요를 누른 유저 정보를 성공적으로 조회했습니다."),
    })
    public BaseResponse<HistoryResponseDTO.LikedUserResults> getLikedUsers(@PathVariable @HistoryExist Long historyId,
                                                                           @Parameter(name = "user", hidden = true) @AuthUser Member member) {

        HistoryResponseDTO.LikedUserResults result = historyService.getLikedUser(member.getId(), historyId);

        return BaseResponse.onSuccess(SuccessStatus.HISTORY_LIKE_USER, result);
    }


    @PostMapping("/{historyId}/comments")
    @Operation(summary = "댓글을 남길 수 있는 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "HISTORY_201", description = "성공적으로 댓글이 생성되었습니다."),
    })
    @Parameters({
            @Parameter(name = "historyId", description = "댓글을 남기고자 하는 기록의 ID")
    })
    public BaseResponse<HistoryResponseDTO.CommentWriteResult> writeComments(@PathVariable @Valid @HistoryExist Long historyId,
                                                                             @Parameter(name = "user", hidden = true) @AuthUser Member member,
                                                                             @RequestBody @Valid HistoryRequestDTO.CommentWrite request) {
        return BaseResponse.onSuccess(SuccessStatus.HISTORY_COMMENT_CREATED, historyService.writeComment(historyId, request.getCommentId(), member.getId(), request.getContent()));

    }

    @PostMapping("/isMyHistory")
    @Operation(summary = "나의 기록인지 확인할 수 있는 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "HISTORY_200", description = "나의 기록인지 성공적으로 조회했습니다."),
    })
    @Parameters({
            @Parameter(name = "historyId", description = "나의 기록인지 확인하고자 하는 기록Id")
    })
    public BaseResponse<HistoryResponseDTO.CheckMyHistoryResult> checkIfHistoryIsMine(@RequestParam @Valid @HistoryExist Long historyId,
                                                                                      @Parameter(name = "user", hidden = true) @AuthUser Member member) {

        HistoryResponseDTO.CheckMyHistoryResult result = historyService.checkIfHistoryIsMine(historyId, member.getId());

        return BaseResponse.onSuccess(SuccessStatus.HISTORY_CHECK_SUCCESS, result);
    }

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "새로운 기록을 생성하는 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "HISTORY_201", description = "CREATED, 성공적으로 생성되었습니다."),
    })
    public BaseResponse<HistoryResponseDTO.HistoryCreateResult> createHistory(
            @RequestPart("historyCreateRequest") @Valid HistoryRequestDTO.HistoryCreate historyCreateRequest,
            @RequestPart(value = "imageFile") @Valid @HistoryImageQuantityLimit List<MultipartFile> imageFiles,
            @Parameter(name = "user", hidden = true) @AuthUser Member member
    ) {

        HistoryResponseDTO.HistoryCreateResult result = historyService.createHistory(historyCreateRequest, member.getId(), imageFiles);

        return BaseResponse.onSuccess(SuccessStatus.HISTORY_CREATED, result);
    }

    @DeleteMapping(value = "/comments/{commentId}")
    @Operation(summary = "댓글을 삭제하는 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "HISTORY_204", description = "댓글이 성공적으로 삭제되었습니다."),
    })
    @Parameters({
            @Parameter(name = "commentId", description = "삭제하고자 하는 댓글의 ID")
    })
    public BaseResponse<Void> deleteComment(
            @Parameter(name = "user", hidden = true) @AuthUser Member member,
            @PathVariable Long commentId
    ) {

        historyService.deleteComment(commentId, member.getId());

        return BaseResponse.onSuccess(SuccessStatus.HISTORY_COMMENT_DELETED, null);
    }

    @PatchMapping(value = "/comments/{commentId}")
    @Operation(summary = "댓글을 수정하는 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "HISTORY_204", description = "댓글이 성공적으로 수정되었습니다."),
    })
    @Parameters({
            @Parameter(name = "commentId", description = "수정하고자 하는 댓글의 ID")
    })
    public BaseResponse<Void> updateComment(
            @RequestBody @Valid HistoryRequestDTO.UpdateComment updateCommentRequest,
            @Parameter(name = "user", hidden = true) @AuthUser Member member,
            @PathVariable Long commentId
    ) {

        historyService.updateComment(updateCommentRequest, commentId, member.getId());

        return BaseResponse.onSuccess(SuccessStatus.HISTORY_COMMENT_UPDATED, null);
    }

    @DeleteMapping(value = "/{historyId}")
    @Operation(summary = "기록을 삭제하는 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "HISTORY_204", description = "기록이 성공적으로 삭제되었습니다."),
    })
    @Parameters({
            @Parameter(name = "historyId", description = "삭제하고자 하는 기록의 ID입니다.")
    })
    public BaseResponse<Void> deleteHistory(
            @Parameter(name = "user", hidden = true) @AuthUser Member member,
            @PathVariable @HistoryExist Long historyId
    ) {

        historyService.deleteHistory(historyId, member.getId());

        return BaseResponse.onSuccess(SuccessStatus.HISTORY_DELETED, null);
    }

    @GetMapping("/liked")
    @Operation(summary = "내가 좋아요 한 기록 전체 조회 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "HISTORY_200", description = "OK, 성공적으로 조회되었습니다.")
    })
    @Parameters({
            @Parameter(name = "page", description = "페이징 관련 query parameter. 1부터 시작합니다.")
    })
    public BaseResponse<HistoryResponseDTO.HistoryLikedListResult> getLikedHistories(@Parameter(name = "user", hidden = true) @AuthUser Member member,
                                                                                     @RequestParam(value = "page") @Valid @CheckPage int page) {
        HistoryResponseDTO.HistoryLikedListResult result = historyService.getLikedHistories(member.getId(), page - 1);

        return BaseResponse.onSuccess(SuccessStatus.HISTORY_SUCCESS, result);
    }

    @GetMapping("/my-comments")
    @Operation(summary = "내가 남긴 댓글 조회 api")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "HISTORY_200", description = "OK, 성공적으로 조회되었습니다.")
    })
    @Parameters({
            @Parameter(name = "page", description = "페이징 관련 query parameter. 1부터 시작합니다.")
    })
    public BaseResponse<HistoryResponseDTO.HistoryMyCommentListResult> getMyComments(@Parameter(name = "user", hidden = true) @AuthUser Member member,
                                                                                     @RequestParam(value = "page") @Valid @CheckPage int page) {
        HistoryResponseDTO.HistoryMyCommentListResult result = historyService.getMyComments(member.getId(), page - 1);

        return BaseResponse.onSuccess(SuccessStatus.HISTORY_SUCCESS, result);
    }
}
