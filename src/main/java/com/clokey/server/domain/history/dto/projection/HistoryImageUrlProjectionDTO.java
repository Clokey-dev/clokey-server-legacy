package com.clokey.server.domain.history.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HistoryImageUrlProjectionDTO {
    private String url;
}
