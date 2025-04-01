package com.clokey.server.domain.cloth.dto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import lombok.*;

import com.clokey.server.domain.model.entity.enums.Season;
import com.clokey.server.domain.model.entity.enums.ThicknessLevel;
import com.clokey.server.domain.model.entity.enums.Visibility;

public class ClothResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClothCreateResult {
        private Long id;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClothPopupViewResult {
        private Long id;
        private Date regDate;
        private String dayOfWeek;
        private String imageUrl;
        private String name;
        private List<Season> seasons;
        private int wearNum;
        private Visibility visibility;
        private String brand;
        private String clothUrl;
        private String category;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClothEditViewResult {
        private Long id;
        private String name;
        private List<Season> seasons;
        private int tempUpperBound;
        private int tempLowerBound;
        private ThicknessLevel thicknessLevel;
        private Visibility visibility;
        private String clothUrl;
        private String brand;
        private String imageUrl;
        private Long categoryId;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClothDetailViewResult {
        private Long id;
        private String name;
        private int wearNum;
        private List<Season> seasons;
        private int tempUpperBound;
        private int tempLowerBound;
        private ThicknessLevel thicknessLevel;
        private Visibility visibility;
        private String clothUrl;
        private String brand;
        private String imageUrl;
        private Long memberId;
        private Long categoryId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClothPreview {
        private Long id;
        private String name;
        private String imageUrl;
        private int wearNum;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClothPreviewListResult {
        private List<ClothPreview> clothPreviews;
        private int totalPage;
        private long totalElements;
        private Boolean isFirst;
        private Boolean isLast;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClosetViewResult {
        private String nickname;
        private ClothPreviewListResult clothPreviewListResult;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SmartSummaryClothPreview {
        private String baseCategoryName;
        private String coreCategoryName;
        private Long coreCategoryId;
        private Long usage;
        private List<ClothPreview> clothPreviews;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SmartSummaryClothPreviewListResult {
        private String nickname;
        private SmartSummaryClothPreview frequentResult;
        private SmartSummaryClothPreview infrequentResult;
    }
}
