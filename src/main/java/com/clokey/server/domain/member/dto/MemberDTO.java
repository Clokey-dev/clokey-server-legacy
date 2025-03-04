package com.clokey.server.domain.member.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.clokey.server.domain.member.exception.annotation.EssentialFieldNotNull;
import com.clokey.server.domain.model.entity.enums.Visibility;

public class MemberDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetUserRP {
        String clokeyId;
        String profileImageUrl;
        Long recordCount;
        Long followerCount;
        Long followingCount;
        String nickname;
        String bio;
        String profileBackImageUrl;
        String visibility;
        List<GetUserClothResult> clothResults;
        Boolean isFollowing;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetUserClothResult {
        Long clothId;
        String clothImage;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProfileRQ {
        @EssentialFieldNotNull
        String nickname;
        @EssentialFieldNotNull
        String clokeyId;
        private String bio;
        Visibility visibility;
    }


    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileRP {
        Long id;
        String bio;
        String email;
        String nickname;
        String clokeyId;
        String profileImageUrl;
        String profileBackImageUrl;
        Visibility visibility;
        LocalDateTime updatedAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfilePreview {
        String nickname;
        String clokeyId;
        String profileImage;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfilePreviewListRP {
        private List<ProfilePreview> profilePreviews;
        private int totalPage;
        private long totalElements;
        private Boolean isFirst;
        private Boolean isLast;
    }


    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FollowRP {
        Boolean isFollow;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetFollowMemberResult {
        List<FollowMemberResult> members;
        private int totalPage;
        private long totalElements;
        private Boolean isFirst;
        private Boolean isLast;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FollowMemberResult {
        String nickname;
        String clokeyId;
        String profileImage;
        Boolean isFollowed;
        Boolean isMe;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class checkMyselfResult {
        Boolean isMe;
    }

}
