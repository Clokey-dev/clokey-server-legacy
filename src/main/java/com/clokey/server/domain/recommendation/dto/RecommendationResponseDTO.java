package com.clokey.server.domain.recommendation.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class RecommendationResponseDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyClothesResult {
        List<DailyClothResult> recommendations;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyClothResult {
        Long clothId;
        String imageUrl;
        String clothName;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyNewsResult {
        private List<RecommendResult> recommend;
        private List<ClosetResult> closet;
        private List<CalendarResult> calendar;
        private List<PeopleResult> people;
        Integer followingCount;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyNewsAllResult<T> {
        private List<T> dailyNewsResult;
        private Integer totalPage;
        private Integer totalElements;
        private Boolean isFirst;
        private Boolean isLast;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendResult {
        private String imageUrl;
        private String subTitle;
        private String hashtag;
        private LocalDateTime date;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendCacheResult {
        private String imageUrl;
        private Long memberId;
        private String subTitle;
        private String hashtag;
        private LocalDateTime date;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClosetResult {
        private String clokeyId;
        private String profileImage;
        private List<Long> clothesId;
        private List<String> images;
        private LocalDate date;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClosetCacheResult {
        private Long memberId;
        private List<Long> clothesId;
        private List<String> images;
        private LocalDate date;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalendarResult {
        private LocalDate date;
        private String clokeyId;
        private String profileImage;
        private Long historyId;
        private String imageUrl;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalendarCacheResult {
        private Long memberId;
        private LocalDate date;
        private Long historyId;
        private String imageUrl;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeopleResult {
        private String clokeyId;
        private String profileImage;
        private String imageUrl;
        private Long historyId;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeopleCacheResult {
        private Long memberId;
        private String imageUrl;
        private Long historyId;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LastYearHistoryResult{
        Boolean isMine;
        Long historyId;
        String nickName;
        LocalDate date;
        List<String> imageUrls;
    }
}
