package com.clokey.server.domain.history.dto;

import com.clokey.server.domain.model.entity.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class HistoryProjectionDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class monthlyHistory {
        Long id;
        LocalDate historyDate;
        Visibility visibility;
    }
}
