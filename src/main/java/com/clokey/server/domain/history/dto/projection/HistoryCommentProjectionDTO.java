package com.clokey.server.domain.history.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HistoryCommentProjectionDTO {
    private Long commentId;
    private String content;
    private boolean isRoot;
    private Long parentId;
    private String clokeyId;
    private String nickname;
    private String profileImageUrl;
    private LocalDateTime createdAt;
}

