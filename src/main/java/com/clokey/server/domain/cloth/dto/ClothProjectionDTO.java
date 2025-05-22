package com.clokey.server.domain.cloth.dto;

import com.clokey.server.domain.model.entity.enums.Season;
import com.clokey.server.domain.model.entity.enums.ThicknessLevel;
import com.clokey.server.domain.model.entity.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClothProjectionDTO {

    private Long id;
    private String name;
    private int wearNum;
    private List<Season> seasons;
    private int tempUpperBound;
    private int tempLowerBound;
    private ThicknessLevel thicknessLevel;
    private Visibility visibility;
    private String clothUrl;
    private String brand;
    private Long categoryId;
    private Long memberId;
    private String imageUrl;
}

