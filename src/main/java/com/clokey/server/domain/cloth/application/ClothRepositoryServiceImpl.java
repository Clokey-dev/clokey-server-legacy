package com.clokey.server.domain.cloth.application;

import com.clokey.server.domain.history.dto.projection.DailyHistoryClothProjectionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.cloth.domain.entity.Cloth;
import com.clokey.server.domain.cloth.domain.repository.ClothRepository;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.model.entity.enums.ClothSort;
import com.clokey.server.domain.model.entity.enums.Season;
import com.clokey.server.domain.model.entity.enums.SummaryFrequency;
import com.clokey.server.domain.model.entity.enums.Visibility;
import com.clokey.server.global.error.code.status.ErrorStatus;
import com.clokey.server.global.error.exception.DatabaseException;

@Service
@RequiredArgsConstructor
public class ClothRepositoryServiceImpl implements ClothRepositoryService{

    private final ClothRepository clothRepository;

    @Override
    public boolean existsById(Long clothId) {
        return clothRepository.existsById(clothId);
    }

    @Override
    public Cloth save(Cloth cloth) {
        return clothRepository.save(cloth);
    }

    @Override
    @EntityGraph(attributePaths = {"images"})
    public Cloth findById(Long id){
        return clothRepository.findById(id).orElseThrow(()->new DatabaseException(ErrorStatus.NO_SUCH_CLOTH));
    }

    @Override
    public List<Cloth> findAll(){
        return clothRepository.findAll();
    }

    @Override
    public List<Cloth> findAllById(List<Long> clothIds) {
        return clothRepository.findAllById(clothIds);
    }

    @Override
    public Page<Cloth> findByClosetFilters(
            @Param("ownerClokeyId") String ownerClokeyId,
            @Param("requesterId") Long requesterId,
            @Param("categoryId") Long categoryId,
            @Param("season") Season season,
            @Param("sort") ClothSort sort,
            Pageable pageable
    ){
        return clothRepository.findByClosetFilters(ownerClokeyId, requesterId, categoryId, season, sort.toString(), pageable);
    }

    @Override
    public List<Cloth> findBySmartSummaryFilters(
            @Param("type") SummaryFrequency type,
            @Param("memberId") Long memberId,
            @Param("categoryId") Long categoryId
    ){
        return switch (type) {
            case FREQUENT -> clothRepository.findMostFrequentClothList(memberId,categoryId);
            case INFREQUENT -> clothRepository.findLeastFrequentClothList(memberId,categoryId);
        };
    }

    @Override
    public Page<Cloth> findByMemberInAndVisibilityOrderByCreatedAtDesc(List<Member> members, Visibility visibility, Pageable pageable) {
        return clothRepository.findByMemberInAndVisibilityOrderByCreatedAtDesc(members, visibility, pageable);
    }

    @Override
    public List<Cloth> findTop6ByMemberInAndVisibilityOrderByCreatedAtDesc(List<Member> members, Visibility visibility) {
        return clothRepository.findTop6ByMemberInAndVisibilityAndCreatedAtBetweenOrderByCreatedAtDesc(members, visibility, LocalDateTime.now().minusWeeks(2), LocalDateTime.now());
    }

    @Override
    public List<Cloth> findBySuitableClothFilters(Long memberId, Integer nowTemp, Integer minTemp, Integer maxTemp) {
        return clothRepository.findBySuitableClothFilters(memberId, nowTemp, minTemp, maxTemp);
    }

    @Override
    public void deleteById(Long id){
        clothRepository.deleteById(id);
    }

    @Override
    public void deleteByClothIds(List<Long> clothIds) {
        clothRepository.deleteByClothIds(clothIds);
    }

    @Override
    public List<Cloth> getTop3Cloths(Member member){
        List<Cloth> cloths = clothRepository.getTop3Cloths(member);
        while (cloths.size() < 3) {
            cloths.add(null);
        }
        return cloths;
    }

    @Override
    public List<Cloth> getTop3PublicCloths(Member member) {
        List<Cloth> cloths = clothRepository.getTop3PublicCloths(member);
        while (cloths.size() < 3) {
            cloths.add(null);
        }
        return cloths;
    }

    @Override
    public List<DailyHistoryClothProjectionDTO> getDailyHistoryClothProjectionsDTO(Long historyId) {
        return clothRepository.getDailyHistoryClothProjectionDTO(historyId);
    }

    @Override
    public List<Long> getClothOwners(List<Long> clothIds) {
        return clothRepository.findMemberIdsByClothIds(clothIds);
    }

}
