package com.clokey.server.domain.history.dto.projection;

import com.clokey.server.domain.model.entity.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HistoryAccessCheckProjectionDTO {
    private Long writerId;
    private Visibility visibility;
}
