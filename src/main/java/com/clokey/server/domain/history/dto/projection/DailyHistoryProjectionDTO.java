package com.clokey.server.domain.history.dto.projection;

import com.clokey.server.domain.model.entity.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DailyHistoryProjectionDTO {
    private Long historyId;
    private String historyContent;
    private Visibility visibility;
    private LocalDate historyDate;
    private Long memberId;
}
