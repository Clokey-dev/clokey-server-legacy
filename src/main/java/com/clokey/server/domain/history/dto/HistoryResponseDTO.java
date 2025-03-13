package com.clokey.server.domain.history.dto;


import com.clokey.server.domain.history.converter.HashtagDeserializer;
import com.clokey.server.domain.history.converter.HashtagSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

public class HistoryResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryPreview {
        Long id;
        String imageUrl;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckMyHistoryResult {
        Boolean isMyHistory;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryPreviewListResult {
        private List<HistoryPreview> historyPreviews;
        private int totalPage;
        private long totalElements;
        private Boolean isFirst;
        private Boolean isLast;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyHistoryResult {
        Long memberId;
        Long historyId;
        String memberImageUrl;
        String nickName;
        String clokeyId;
        String contents;
        List<String> imageUrl;
        @JsonSerialize(using = HashtagSerializer.class)
        @JsonDeserialize(using = HashtagDeserializer.class)
        List<String> hashtags;
        boolean visibility;
        int likeCount;
        boolean isLiked;
        LocalDate date;
        List<HistoryClothResult> cloths;
        Long commentCount;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryClothResult {
        Long clothId;
        String clothImageUrl;
        String clothName;
    }


    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthViewResult {
        Long memberId;
        String nickName;
        List<HistoryResult> histories;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryResult {
        Long historyId;
        LocalDate date;
        String imageUrl;
    }


    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryCommentResult {
        List<CommentResult> comments;
        int totalPage;
        int totalElements;

        @JsonProperty("isFirst") // JSON 직렬화 시 "isFirst" 사용
        private boolean isFirst;

        @JsonIgnore // "first" 필드 직렬화 방지
        public boolean isFirst() {
            return isFirst;
        }

        @JsonProperty("isLast") // JSON 직렬화 시 "isLast" 사용
        private boolean isLast;

        @JsonIgnore // "last" 필드 직렬화 방지
        public boolean isLast() {
            return isLast;
        }
    }


    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonPropertyOrder({"commentId", "clokeyId", "nickName", "userImageUrl", "content", "replyResults"})
    public static class CommentResult {
        Long commentId;
        String clokeyId;
        String nickName;
        String userImageUrl;
        String content;
        List<ReplyResult> replyResults;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReplyResult {
        Long commentId;
        String clokeyId;
        String nickName;
        String userImageUrl;
        String content;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LikeResult {
        Long historyId;
        boolean isLiked;
        int likeCount;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LikedUserResults {
        List<LikedUserResult> likedUsers;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LikedUserResult {
        Long memberId;
        String clokeyId;
        String imageUrl;
        String nickname;
        boolean followStatus;
        boolean isMe;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentWriteResult {
        Long commentId;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryCreateResult {
        Long historyId;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryMyCommentListResult {
        private List<HistoryMyCommentResult> histories;
        private int totalPage;
        private long totalElements;
        private Boolean isFirst;
        private Boolean isLast;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryMyCommentResult {
        private List<MyCommentResult> comments;
        private Long historyId;
        private String imageUrl;
        private LocalDate date;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyCommentResult {
        private String content;
    }
}
