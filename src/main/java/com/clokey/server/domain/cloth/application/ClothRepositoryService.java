package com.clokey.server.domain.cloth.application;

import com.clokey.server.domain.history.dto.projection.DailyHistoryClothProjectionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import com.clokey.server.domain.cloth.domain.entity.Cloth;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.model.entity.enums.ClothSort;
import com.clokey.server.domain.model.entity.enums.Season;
import com.clokey.server.domain.model.entity.enums.SummaryFrequency;
import com.clokey.server.domain.model.entity.enums.Visibility;

public interface ClothRepositoryService {

    Cloth findById(Long id);

    void deleteById(Long id);

    Cloth save(Cloth cloth);

    boolean existsById(Long clothId);

    Page<Cloth> findByClosetFilters(
            String clokeyId,
            Long requesterId,
            Long categoryId,
            Season season,
            ClothSort sort,
            Pageable pageable
    );

    List<Cloth> findBySmartSummaryFilters(
            SummaryFrequency type,
            Long memberId,
            Long categoryId
    );

    List<Cloth> findAllById(List<Long> clothIds);

    void deleteByClothIds(List<Long> clothIds);

    List<Cloth> findAll();

    Page<Cloth> findByMemberInAndVisibilityOrderByCreatedAtDesc(
            List<Member> members, Visibility visibility, Pageable pageable);

    List<Cloth> findTop6ByMemberInAndVisibilityOrderByCreatedAtDesc(
            List<Member> members, Visibility visibility);

    List<Cloth> findBySuitableClothFilters(Long memberId, Integer nowTemp, Integer minTemp, Integer maxTemp);

    List<Cloth> getTop3Cloths(Member member);

    List<Cloth> getTop3PublicCloths(Member member);

    List<DailyHistoryClothProjectionDTO> getDailyHistoryClothProjectionsDTO(Long historyId);

    List<Long> getClothOwners(List<Long> clothIds);
}
