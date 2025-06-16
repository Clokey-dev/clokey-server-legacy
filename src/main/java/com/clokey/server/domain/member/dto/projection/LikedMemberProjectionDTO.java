package com.clokey.server.domain.member.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LikedMemberProjectionDTO {
    private Long memberId;
    private String clokeyId;
    private String imageUrl;
    private String nickname;
}
