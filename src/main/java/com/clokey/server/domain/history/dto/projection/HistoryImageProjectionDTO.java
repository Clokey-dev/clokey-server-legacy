package com.clokey.server.domain.history.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HistoryImageProjectionDTO {

    private Long id;
    private String imageUrl;
    private Long historyId;

}
