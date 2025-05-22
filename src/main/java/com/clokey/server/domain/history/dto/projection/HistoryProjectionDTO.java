package com.clokey.server.domain.history.dto.projection;

import com.clokey.server.domain.model.entity.enums.Visibility;
import lombok.*;

import java.time.LocalDate;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HistoryProjectionDTO {

    private Long id;
    private LocalDate historyDate;
    private int likes;
    private Visibility visibility;
    private String content;
    private boolean banned;
    private Long memberId;
}
