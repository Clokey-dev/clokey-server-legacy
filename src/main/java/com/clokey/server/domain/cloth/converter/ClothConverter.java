package com.clokey.server.domain.cloth.converter;

import org.springframework.data.domain.Page;

import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

import com.clokey.server.domain.category.domain.entity.Category;
import com.clokey.server.domain.cloth.domain.document.ClothDocument;
import com.clokey.server.domain.cloth.domain.entity.Cloth;
import com.clokey.server.domain.cloth.domain.entity.ClothImage;
import com.clokey.server.domain.cloth.dto.ClothRequestDTO;
import com.clokey.server.domain.cloth.dto.ClothResponseDTO;
import com.clokey.server.domain.member.domain.entity.Member;

public class ClothConverter {

    public static ClothResponseDTO.ClothCreateResult toClothCreateResult(Cloth cloth) {
        return ClothResponseDTO.ClothCreateResult.builder()
                .id(cloth.getId())
                .build();
    }

    public static ClothResponseDTO.ClothPopupViewResult toClothPopupViewResult(Cloth cloth) {

        // CreatedAt으로 등록일자 가져오기 / LocalDateTime -> Date 변환
        Date regDate = java.sql.Date.valueOf(cloth.getCreatedAt().toLocalDate());

        // 요일 가져오기
        String dayOfWeek = cloth.getCreatedAt().getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                .toUpperCase(); // 대문자로 출력

        return ClothResponseDTO.ClothPopupViewResult.builder()
                .id(cloth.getId())
                .regDate(regDate)
                .dayOfWeek(dayOfWeek)
                .imageUrl(cloth.getImage() != null ? cloth.getImage().getImageUrl() : null)
                .name(cloth.getName())
                .seasons(cloth.getSeasons())
                .wearNum(cloth.getWearNum())
                .visibility(cloth.getVisibility())
                .brand(cloth.getBrand())
                .clothUrl(cloth.getClothUrl())
                .category(cloth.getCategory().getName())
                .build();
    }

    public static ClothResponseDTO.ClothEditViewResult toClothEditViewResult(Cloth cloth) {

        return ClothResponseDTO.ClothEditViewResult.builder()
                .id(cloth.getId())
                .name(cloth.getName())
                .seasons(cloth.getSeasons())
                .tempUpperBound(cloth.getTempUpperBound())
                .tempLowerBound(cloth.getTempLowerBound())
                .thicknessLevel(cloth.getThicknessLevel())
                .visibility(cloth.getVisibility())
                .clothUrl(cloth.getClothUrl())
                .brand(cloth.getBrand())
                .imageUrl(cloth.getImage() != null ? cloth.getImage().getImageUrl() : null)
                .categoryId(cloth.getCategory().getId())
                .build();
    }

    public static ClothResponseDTO.ClothDetailViewResult toClothDetailViewResult(Cloth cloth) {

        return ClothResponseDTO.ClothDetailViewResult.builder()
                .id(cloth.getId())
                .name(cloth.getName())
                .wearNum(cloth.getWearNum())
                .seasons(cloth.getSeasons())
                .tempUpperBound(cloth.getTempUpperBound())
                .tempLowerBound(cloth.getTempLowerBound())
                .thicknessLevel(cloth.getThicknessLevel())
                .visibility(cloth.getVisibility())
                .clothUrl(cloth.getClothUrl())
                .brand(cloth.getBrand())
                .imageUrl(cloth.getImage() != null ? cloth.getImage().getImageUrl() : null)
                .memberId(cloth.getMember().getId())
                .categoryId(cloth.getCategory().getId())
                .createdAt(cloth.getCreatedAt())
                .updatedAt(cloth.getUpdatedAt())
                .build();
    }

    public static List<ClothResponseDTO.ClothPreview> toClothPreviewList(List<Cloth> clothes) {
        return clothes.stream()
                .map(cloth -> ClothResponseDTO.ClothPreview.builder()
                        .id(cloth.getId())
                        .name(cloth.getName())
                        .imageUrl(cloth.getImage() != null ? cloth.getImage().getImageUrl() : null)
                        .wearNum(cloth.getWearNum())
                        .build()
                ).collect(Collectors.toList());
    }

    public static <T> List<ClothResponseDTO.ClothPreview> toClothPreviewList(Page<? extends T> page) {
        return Optional.ofNullable(page.getContent()).orElse(Collections.emptyList())
                .stream()
                .map(item -> {
                    if (item instanceof Cloth) {
                        Cloth cloth = (Cloth) item;
                        return ClothResponseDTO.ClothPreview.builder()
                                .id(cloth.getId())
                                .name(cloth.getName())
                                .imageUrl(Optional.ofNullable(cloth.getImage())
                                        .map(ClothImage::getImageUrl)
                                        .orElse(null))
                                .wearNum(Optional.ofNullable(cloth.getWearNum()).orElse(0))
                                .build();
                    } else if (item instanceof ClothDocument) {
                        ClothDocument doc = (ClothDocument) item;
                        return ClothResponseDTO.ClothPreview.builder()
                                .id(doc.getId())
                                .name(doc.getName())
                                .imageUrl(Optional.ofNullable(doc.getImageUrl()).orElse(null))
                                .wearNum(Optional.ofNullable(doc.getWearNum()).orElse(0))
                                .build();
                    }
                    throw new IllegalArgumentException("Unsupported type: " + item.getClass());
                })
                .collect(Collectors.toList());
    }

    public static ClothResponseDTO.ClosetViewResult toClosetViewResult(String nickname, Page<?> page,
                                                                       List<ClothResponseDTO.ClothPreview> clothPreviews) {
        return ClothResponseDTO.ClosetViewResult.builder()
                .nickname(nickname)
                .clothPreviews(clothPreviews)
                .totalPage(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }

    public static ClothResponseDTO.ClothPreviewListResult toClothPreviewListResult(Page<?> page,
                                                                                   List<ClothResponseDTO.ClothPreview> clothPreviews) {
        return ClothResponseDTO.ClothPreviewListResult.builder()
                .clothPreviews(clothPreviews)
                .totalPage(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }

    public static ClothResponseDTO.SmartSummaryClothPreviewListResult toSummaryClothPreviewListResult(
            String nickname,
            Category frequentCategory,
            Category infrequentCategory,
            Long frequentUsage,
            Long infrequentUsage,
            List<ClothResponseDTO.ClothPreview> frequentClothPreviews,
            List<ClothResponseDTO.ClothPreview> infrequentClothPreviews
    ) {
        return ClothResponseDTO.SmartSummaryClothPreviewListResult.builder()
                .nickname(nickname)
                .frequentResult(
                        ClothResponseDTO.SmartSummaryClothPreview.builder()
                                .baseCategoryName(frequentCategory.getParent().getName())
                                .coreCategoryName(frequentCategory.getName())
                                .coreCategoryId(frequentCategory.getId())
                                .usage(frequentUsage)
                                .clothPreviews(frequentClothPreviews)
                                .build()
                )
                .infrequentResult(
                        ClothResponseDTO.SmartSummaryClothPreview.builder()
                                .baseCategoryName(infrequentCategory.getParent().getName())
                                .coreCategoryName(infrequentCategory.getName())
                                .coreCategoryId(infrequentCategory.getId())
                                .usage(infrequentUsage)
                                .clothPreviews(infrequentClothPreviews)
                                .build()
                )
                .build();
    }

    public static Cloth toCloth(Long memberId, ClothRequestDTO.ClothCreateOrUpdateRequest request) {
        return Cloth.builder()
                .name(request.getName())
                .seasons(request.getSeasons()) // List<Season> 직접 할당
                .tempUpperBound(request.getTempUpperBound())
                .tempLowerBound(request.getTempLowerBound())
                .thicknessLevel(request.getThicknessLevel())
                .visibility(request.getVisibility())
                .clothUrl(request.getClothUrl())
                .brand(request.getBrand())
                .category(Category.builder()
                        .id(request.getCategoryId())
                        .build()) // Category를 간단히 생성 (참조만 설정)
                .member(Member.builder()
                        .id(memberId)
                        .build()) // Member를 간단히 생성 (참조만 설정)
                .build();
    }
}
