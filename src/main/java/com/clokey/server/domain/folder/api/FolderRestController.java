package com.clokey.server.domain.folder.api;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.folder.application.FolderService;
import com.clokey.server.domain.folder.converter.FolderConverter;
import com.clokey.server.domain.folder.dto.FolderRequestDTO;
import com.clokey.server.domain.folder.dto.FolderResponseDTO;
import com.clokey.server.domain.folder.exception.annotation.FolderExist;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.member.exception.annotation.AuthUser;
import com.clokey.server.global.common.response.BaseResponse;
import com.clokey.server.global.error.code.status.SuccessStatus;
import com.clokey.server.global.error.exception.annotation.CheckPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Validated
@RestController
@RequiredArgsConstructor
public class FolderRestController {

    private final FolderService folderService;

    @Operation(summary = "폴더 생성 및 수정 API", description = "폴더 생성 및 수정하는 API입니다.")
    @PostMapping("/folders")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "FOLDER_200", description = "성공적으로 반영되었습니다."),
    })
    public BaseResponse<FolderResponseDTO.FolderIdResult> createAndUpdateFolder(@Parameter(name = "user", hidden = true) @AuthUser Member member,
                                                                                @RequestBody @Valid FolderRequestDTO.FolderCreateRequest request) {
        FolderResponseDTO.FolderIdResult response = FolderConverter.toFolderIdDTO(folderService.createAndUpdateFolder(member.getId(), request));
        return BaseResponse.onSuccess(SuccessStatus.FOLDER_CLOTHES_SUCCESS, response);
    }

    @Operation(summary = "폴더 삭제 API", description = "폴더 삭제하는 API입니다.")
    @DeleteMapping("/folders/{folderId}")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "FOLDER_204", description = "성공적으로 삭제되었습니다."),
    })
    public BaseResponse<String> deleteFolder(@Parameter(name = "user", hidden = true) @AuthUser Member member,
                                             @FolderExist @PathVariable Long folderId) {
        folderService.deleteFolder(folderId, member.getId());
        return BaseResponse.onSuccess(SuccessStatus.FOLDER_DELETED, null);
    }

    @Operation(summary = "폴더별 옷 조회 API", description = "폴더별 옷 조회하는 API입니다.")
    @GetMapping("folders/{folderId}/clothes")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "FOLDER_200", description = "성공적으로 조회되었습니다."),
    })
    public BaseResponse<FolderResponseDTO.FolderClothesResult> getClothesFromFolder(@Parameter(name = "user", hidden = true) @AuthUser Member member,
                                                                                    @PathVariable @FolderExist Long folderId,
                                                                                    @RequestParam(value = "page") @Valid @CheckPage Integer page) {
        FolderResponseDTO.FolderClothesResult result = folderService.getClothesFromFolder(folderId, page, member.getId());
        return BaseResponse.onSuccess(SuccessStatus.FOLDER_SUCCESS, result);
    }

    @Operation(summary = "전체 폴더 조회 API", description = "사용자가 가지고 있는 전체 폴더를 조회하는 API입니다.")
    @GetMapping("folders")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "FOLDER_200", description = "성공적으로 조회되었습니다."),
    })
    public BaseResponse<FolderResponseDTO.FoldersResult> getFolders(@Parameter(name = "user", hidden = true) @AuthUser Member member,
                                                                    @RequestParam(value = "page") @Valid @CheckPage Integer page) {
        FolderResponseDTO.FoldersResult result = folderService.getFolders(page, member.getId());
        return BaseResponse.onSuccess(SuccessStatus.FOLDER_SUCCESS, result);
    }
}
