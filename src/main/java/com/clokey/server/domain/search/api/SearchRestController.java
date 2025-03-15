package com.clokey.server.domain.search.api;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.cloth.dto.ClothResponseDTO;
import com.clokey.server.domain.history.dto.HistoryResponseDTO;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.member.dto.MemberDTO;
import com.clokey.server.domain.member.exception.annotation.AuthUser;
import com.clokey.server.domain.member.exception.annotation.NullableClokeyIdExist;
import com.clokey.server.domain.member.exception.validator.MemberAccessibleValidator;
import com.clokey.server.domain.search.application.SearchRepositoryService;
import com.clokey.server.domain.search.application.SearchService;
import com.clokey.server.domain.search.exception.annotation.KeywordNotNull;
import com.clokey.server.global.common.response.BaseResponse;
import com.clokey.server.global.error.code.status.ErrorStatus;
import com.clokey.server.global.error.code.status.SuccessStatus;
import com.clokey.server.global.error.exception.annotation.CheckPage;
import com.clokey.server.global.error.exception.annotation.CheckPageSize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
@Validated
public class SearchRestController {

    private final SearchService searchService;
    private final SearchRepositoryService searchRepositoryService;
    private final MemberAccessibleValidator memberAccessibleValidator;

    private final String CLOTH_SEARCH_FILTER_BY_NAME_AND_BRAND= "name-and-brand";
    private final String HISTORY_SEARCH_FILTER_BY_HASHTAG_AND_CATEGORY= "hashtag-and-category";
    private final String USER_SEARCH_FILTER_BY_ID_AND_NICKNAME= "id-and-nickname";

    /****************************************Sync****************************************/

    // 옷 Elastic Search 동기화 API
    @PostMapping("/clothes/sync")
    @Operation(summary = "옷 데이터를 Elastic Search에 동기화 시키는 API (관리자용)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "SEARCH_201", description = "OK, 성공적으로 생성되었습니다."),
    })
    public BaseResponse<Void> syncClothData() throws IOException {
        searchRepositoryService.syncAllClothesDataToElasticsearch();
        return BaseResponse.onSuccess(SuccessStatus.CLOTH_SYNC_CREATED, null);
    }

    // 기록 Elastic Search 동기화 API
    @PostMapping("/histories/sync")
    @Operation(summary = "기록 데이터를 Elastic Search에 동기화 시키는 API (관리자용)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "SEARCH_201", description = "OK, 성공적으로 생성되었습니다."),
    })
    public BaseResponse<Void> syncHistoryData() throws IOException {
        searchRepositoryService.syncAllHistoriesDataToElasticsearch();
        return BaseResponse.onSuccess(SuccessStatus.HISTORY_SYNC_CREATED, null);
    }

    // 유저 Elastic Search 동기화 API
    @PostMapping("/members/sync")
    @Operation(summary = "유저 데이터를 Elastic Search에 동기화 시키는 API (관리자용)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "SEARCH_201", description = "OK, 성공적으로 생성되었습니다."),
    })
    public BaseResponse<Void> syncUserData() throws IOException {
        searchRepositoryService.syncAllMembersDataToElasticsearch();
        return BaseResponse.onSuccess(SuccessStatus.MEMBER_SYNC_CREATED, null);
    }

    /****************************************Search****************************************/

    // 옷 검색 API
    @GetMapping("/clothes")
    @Operation(summary = "옷 이름과 브랜드 명으로 옷을 검색하는 API", description = "query string으로 by를 넘겨주세요" +
            "query string으로 keyword를 넘겨주세요" +
            "query string으로 by를 넘겨주세요. " +
            "query string으로 page를 넘겨주세요. " +
            "query string으로 pageSize를 넘겨주세요.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "SEARCH_200", description = "OK, 성공적으로 조회되었습니다."),
    })
    @Parameters({
            @Parameter(name = "by", description = "검색 필터(name-and-brand), query string 입니다."),
            @Parameter(name = "keyword", description = "검색할 키워드, query string 입니다."),
            @Parameter(name = "page", description = "페이지 값, query string 입니다."),
            @Parameter(name = "size", description = "페이지에 표시할 요소 개수 값, query string 입니다.")
    })
    public BaseResponse<ClothResponseDTO.ClothPreviewListResult> searchClothes(
            @RequestParam(value = "clokeyId", required = false) @NullableClokeyIdExist String clokeyId,
            @RequestParam String by,
            @RequestParam @KeywordNotNull String keyword,
            @RequestParam @CheckPage int page,
            @RequestParam @CheckPageSize int size,
            @Parameter(name = "user", hidden = true) @AuthUser Member member
    ) {
        if(clokeyId==null || clokeyId.isEmpty()) {
            clokeyId=member.getClokeyId();
        }
        else {
            // 조회하는 유저와 다른 유저의 옷장이고, 그 유저가 비공개인 유저인지 확인합니다.
            memberAccessibleValidator.validateClothAccessOfMember(clokeyId, member.getId());
        }
        if(CLOTH_SEARCH_FILTER_BY_NAME_AND_BRAND.equals(by)) {
            try {
                ClothResponseDTO.ClothPreviewListResult result = searchService.searchClothesByNameOrBrand(member.getId(),clokeyId,keyword,page,size);
                return BaseResponse.onSuccess(SuccessStatus.SEARCH_SUCCESS, result);
            } catch (IOException e) {
                return BaseResponse.onFailure(ErrorStatus.SEARCHING_IOEXCEPION, null);
            }
        }
        else
            return BaseResponse.onFailure(ErrorStatus.SEARCH_FILTER_ERROR, null);
    }

    // 기록 검색 API
    @GetMapping("/histories")
    @Operation(summary = "기록의 해시태그와 태그된 옷의 카테고리로 유저를 검색하는 API", description = "query string으로 by를 넘겨주세요" +
            "query string으로 keyword를 넘겨주세요" +
            "query string으로 by를 넘겨주세요. " +
            "query string으로 page를 넘겨주세요. " +
            "query string으로 pageSize를 넘겨주세요.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "SEARCH_200", description = "OK, 성공적으로 조회되었습니다."),
    })
    @Parameters({
            @Parameter(name = "by", description = "검색 필터(hashtag-and-category), query string 입니다."),
            @Parameter(name = "keyword", description = "검색할 키워드, query string 입니다."),
            @Parameter(name = "page", description = "페이지 값, query string 입니다."),
            @Parameter(name = "size", description = "페이지에 표시할 요소 개수 값, query string 입니다.")
    })
    public BaseResponse<HistoryResponseDTO.HistoryPreviewListResult> searchHistories(
            @RequestParam String by,
            @RequestParam @KeywordNotNull String keyword,
            @RequestParam @CheckPage int page,
            @RequestParam @CheckPageSize int size,
            @Parameter(name = "user", hidden = true) @AuthUser Member member
    ) {
        if(HISTORY_SEARCH_FILTER_BY_HASHTAG_AND_CATEGORY.equals(by)) {
            try {
                HistoryResponseDTO.HistoryPreviewListResult result = searchService.searchHistoriesByHashtagAndCategory(member.getId(),keyword,page,size);
                return BaseResponse.onSuccess(SuccessStatus.SEARCH_SUCCESS, result);
            } catch (IOException e) {
                return BaseResponse.onFailure(ErrorStatus.SEARCHING_IOEXCEPION, null);
            }
        }
        else
            return BaseResponse.onFailure(ErrorStatus.SEARCH_FILTER_ERROR, null);
    }

    // 유저 검색 API
    @GetMapping("/members")
    @Operation(summary = "유저의 클로키ID와 닉네임으로 유저를 검색하는 API", description = "query string으로 by를 넘겨주세요" +
            "query string으로 keyword를 넘겨주세요" +
            "query string으로 by를 넘겨주세요. " +
            "query string으로 page를 넘겨주세요. " +
            "query string으로 pageSize를 넘겨주세요.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "SEARCH_200", description = "OK, 성공적으로 조회되었습니다."),
    })
    @Parameters({
            @Parameter(name = "by", description = "검색 필터(id-and-nickname), query string 입니다."),
            @Parameter(name = "keyword", description = "검색할 키워드, query string 입니다."),
            @Parameter(name = "page", description = "페이지 값, query string 입니다."),
            @Parameter(name = "size", description = "페이지에 표시할 요소 개수 값, query string 입니다.")
    })
    public BaseResponse<MemberDTO.ProfilePreviewListRP> searchMembers(
            @RequestParam String by,
            @RequestParam @KeywordNotNull String keyword,
            @RequestParam @CheckPage int page,
            @RequestParam @CheckPageSize int size,
            @Parameter(name = "user", hidden = true) @AuthUser Member member
    ) {
        if(USER_SEARCH_FILTER_BY_ID_AND_NICKNAME.equals(by)) {
            try {
                MemberDTO.ProfilePreviewListRP result = searchService.searchMembersByClokeyIdOrNickname(member.getId(),keyword,page,size);
                return BaseResponse.onSuccess(SuccessStatus.SEARCH_SUCCESS, result);
            } catch (IOException e) {
                return BaseResponse.onFailure(ErrorStatus.SEARCHING_IOEXCEPION, null);
            }
        }
        else
            return BaseResponse.onFailure(ErrorStatus.SEARCH_FILTER_ERROR, null);
    }
}
