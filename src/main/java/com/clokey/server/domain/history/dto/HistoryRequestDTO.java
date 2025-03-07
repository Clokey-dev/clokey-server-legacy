package com.clokey.server.domain.history.dto;

import java.util.List;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.clokey.server.domain.history.exception.annotation.*;
import com.clokey.server.domain.model.entity.enums.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HistoryRequestDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryCreate {

        @HistoryContentLength
        String content;

        @UniqueClothes
        List<Long> clothes;

        @HashtagCondition
        List<String> hashtags;

        Visibility visibility;

        @HistoryDateFormat
        String date;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LikeStatusChange {

        @HistoryExist
        Long historyId;

        @JsonProperty("liked")
        boolean isLiked;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentWrite {


        @ParentCommentConditions
        Long commentId;

        @CommentContentLength
        @NotBlank
        String content;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateComment {

        @CommentContentLength
        @NotBlank
        String content;
    }


}
