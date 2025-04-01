package com.clokey.server.domain.cloth.application;

import com.clokey.server.domain.cloth.domain.entity.Cloth;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

import com.clokey.server.domain.cloth.dto.ClothRequestDTO;
import com.clokey.server.domain.cloth.dto.ClothResponseDTO;
import com.clokey.server.domain.model.entity.enums.ClothSort;
import com.clokey.server.domain.model.entity.enums.Season;

public interface ClothService {

    ClothResponseDTO.ClothPopupViewResult readClothPopupInfoById(Long clothId);

    ClothResponseDTO.ClothEditViewResult readClothEditInfoById(Long clothId);

    ClothResponseDTO.ClothDetailViewResult readClothDetailInfoById(Long clothId);

    ClothResponseDTO.ClosetViewResult readClothPreviewInfoListByClokeyId(String ownerClokeyId, Long requesterId, Long categoryId, Season season, ClothSort sort, int page, int pageSize);

    ClothResponseDTO.SmartSummaryClothPreviewListResult readSmartSummary(Long memberId);

    ClothResponseDTO.ClothCreateResult createCloth(Long memberId, ClothRequestDTO.ClothCreateOrUpdateRequest request, MultipartFile imageFile);

    void updateClothById(Long clothId, ClothRequestDTO.ClothCreateOrUpdateRequest request, MultipartFile imageFile);

    void deleteClothById(Long clothId);

    @Async
    void asyncUpdatedClothFromES(Cloth cloth);

    @Async
    void asyncDeletedClothFromES(Long clothId);
}
