package com.clokey.server.domain.member.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DailyHistoryMemberProjectionDTO {
    private String profileUrl;
    private String nickname;
    private String clokeyId;
}
