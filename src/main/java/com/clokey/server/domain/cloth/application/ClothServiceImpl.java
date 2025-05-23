package com.clokey.server.domain.cloth.application;

import com.clokey.server.domain.cloth.domain.repository.ClothImageRepository;
import com.clokey.server.domain.cloth.domain.repository.ClothRepository;
import com.clokey.server.domain.cloth.exception.ClothException;
import com.clokey.server.domain.history.domain.repository.HistoryClothRepository;
import com.clokey.server.domain.history.domain.repository.HistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.category.domain.entity.Category;
import com.clokey.server.domain.category.exception.CategoryException;
import com.clokey.server.domain.cloth.converter.ClothConverter;
import com.clokey.server.domain.cloth.domain.entity.Cloth;
import com.clokey.server.domain.cloth.domain.entity.ClothImage;
import com.clokey.server.domain.cloth.dto.ClothRequestDTO;
import com.clokey.server.domain.cloth.dto.ClothResponseDTO;
import com.clokey.server.domain.folder.application.ClothFolderRepositoryService;
import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.member.application.MemberRepositoryService;
import com.clokey.server.domain.model.entity.enums.ClothSort;
import com.clokey.server.domain.model.entity.enums.Season;
import com.clokey.server.domain.model.entity.enums.SummaryFrequency;
import com.clokey.server.domain.search.application.SearchRepositoryService;
import com.clokey.server.domain.search.exception.SearchException;
import com.clokey.server.global.error.code.status.ErrorStatus;
import com.clokey.server.global.infra.s3.S3ImageService;

@Service
@RequiredArgsConstructor
public class ClothServiceImpl implements ClothService {

    private final ClothImageRepository clothImageRepository;
    private final ClothFolderRepositoryService clothFolderRepositoryService;
    private final HistoryClothRepository historyClothRepository;
    private final HistoryRepository historyRepository;
    private final S3ImageService s3ImageService;
    private final SearchRepositoryService searchRepositoryService;
    private final MemberRepositoryService memberRepositoryService;
    private final ClothRepository clothRepository;

    private static final String FAILED_ES_UPDATE_SYNC_CLOTH_KEY = "failed_es_update_sync_cloth";
    private static final String FAILED_ES_DELETE_SYNC_CLOTH_KEY = "failed_es_delete_sync_cloth";

    @Override
    @Transactional(readOnly = true)
    public ClothResponseDTO.ClothPopupViewResult readClothPopupInfoById(Long clothId) {

        Cloth cloth = clothRepository.findById(clothId).orElseThrow(()-> new ClothException(ErrorStatus.NO_SUCH_CLOTH));

        return ClothConverter.toClothPopupViewResult(cloth);
    }

    @Override
    @Transactional(readOnly = true)
    public ClothResponseDTO.ClothEditViewResult readClothEditInfoById(Long clothId){

        Cloth cloth = clothRepository.findById(clothId).orElseThrow(()-> new ClothException(ErrorStatus.NO_SUCH_CLOTH));

        return ClothConverter.toClothEditViewResult(cloth);
    }

    @Override
    @Transactional(readOnly = true)
    public ClothResponseDTO.ClothDetailViewResult readClothDetailInfoById(Long clothId){

        Cloth cloth = clothRepository.findById(clothId).orElseThrow(()-> new ClothException(ErrorStatus.NO_SUCH_CLOTH));

        return ClothConverter.toClothDetailViewResult(cloth);
    }

    // 옷장의 옷의 PreView 조회 후 옷장 조회 DTO로 변환해서 반환
    @Override
    @Transactional(readOnly = true)
    public ClothResponseDTO.ClosetViewResult readClothPreviewInfoListByClokeyId(
            String ownerClokeyId, Long requesterId, Long categoryId, Season season, ClothSort sort, int page, int size) {

        String nickname= memberRepositoryService.findByClokeyId(ownerClokeyId).getNickname();

        Pageable pageable = PageRequest.of(page-1, size);
        Page<Cloth> clothes = clothRepository.findByClosetFilters(ownerClokeyId, requesterId, categoryId, season, sort.toString(), pageable);

        List<ClothResponseDTO.ClothPreview> clothPreviews = ClothConverter.toClothPreviewList(clothes);

        return ClothConverter.toClosetViewResult(nickname, clothes, clothPreviews);
    }

    // 지난 7일간 착용횟수를 통해 카테고리와 카테고리에 해당하는 옷의 PreView 조회 후 스마트 요약 DTO로 변환해서 반환
    @Override
    @Transactional(readOnly = true)
    public ClothResponseDTO.SmartSummaryClothPreviewListResult readSmartSummary(Long memberId) {

        String nickname = memberRepositoryService.findMemberById(memberId).getNickname();

        LocalDate monthAgo = LocalDate.now().minusMonths(1);
        List<History> histories = historyRepository.findHistoriesWithinMonth(memberId, monthAgo);

        List<Cloth> clothes = histories.stream()
                .flatMap(history -> historyClothRepository.findAllClothsByHistoryId(history.getId()).stream())
                .toList();

        Map<Category, Long> categoryCountMap = clothes.stream()
                .collect(Collectors.groupingBy(Cloth::getCategory, Collectors.counting()));

        List<Map.Entry<Category, Long>> filteredEntries = categoryCountMap.entrySet().stream()
                .filter(entry -> entry.getKey().getParent() != null)
                .toList();

        if (filteredEntries.isEmpty()) {
            throw new CategoryException(ErrorStatus.CATEGORY_NOT_FOUND_IN_SUMMARY);
        }

        Map.Entry<Category, Long> frequentEntry = filteredEntries.stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow(() -> new CategoryException(ErrorStatus.CATEGORY_NOT_FOUND_IN_SUMMARY));
        Map.Entry<Category, Long> infrequentEntry = filteredEntries.stream()
                .min(Map.Entry.comparingByValue())
                .orElseThrow(() -> new CategoryException(ErrorStatus.CATEGORY_NOT_FOUND_IN_SUMMARY));

        List<Cloth> frequentClothes = findBySmartSummaryFilters(
                SummaryFrequency.FREQUENT, memberId, frequentEntry.getKey().getId());
        List<ClothResponseDTO.ClothPreview> frequentClothPreviews = ClothConverter.toClothPreviewList(frequentClothes);

        List<Cloth> infrequentClothes = findBySmartSummaryFilters(
                SummaryFrequency.INFREQUENT, memberId, infrequentEntry.getKey().getId());
        List<ClothResponseDTO.ClothPreview> infrequentClothPreviews = ClothConverter.toClothPreviewList(infrequentClothes);

        return ClothConverter.toSummaryClothPreviewListResult(
                nickname,
                frequentEntry.getKey(),           // 자주 입은 카테고리 객체
                infrequentEntry.getKey(),         // 덜 입은 카테고리 객체
                frequentEntry.getValue(),         // 자주 입은 카테고리 착용 횟수
                infrequentEntry.getValue(),       // 덜 입은 카테고리 착용 횟수
                frequentClothPreviews,            // 자주 입은 카테고리의 Cloth PreView 목록
                infrequentClothPreviews           // 덜 입은 카테고리의 Cloth PreView 목록
        );
    }

    @Override
    @Transactional
    public ClothResponseDTO.ClothCreateResult createCloth(Long memberId,
                                                          ClothRequestDTO.ClothCreateOrUpdateRequest request,
                                                          MultipartFile imageFile) {

        Cloth cloth = clothRepository.save(ClothConverter.toCloth(memberId, request));

        String imageUrl = (imageFile != null) ? s3ImageService.upload(imageFile) : null;

        ClothImage clothImage = ClothImage.builder()
                .imageUrl(imageUrl)
                .cloth(cloth)
                .build();

        clothImageRepository.save(clothImage);

        asyncUpdatedClothFromES(cloth);

        return ClothConverter.toClothCreateResult(cloth);
    }

    @Override
    @Transactional
    public void updateClothById(Long clothId,
                                ClothRequestDTO.ClothCreateOrUpdateRequest request,
                                MultipartFile imageFile){

        Cloth existingCloth = clothRepository.findById(clothId).orElseThrow(()-> new ClothException(ErrorStatus.NO_SUCH_CLOTH));

        String imageUrl = (imageFile != null) ? s3ImageService.upload(imageFile) : null;

        existingCloth.updateCloth(
                request.getName(),
                request.getSeasons(),
                request.getTempUpperBound(),
                request.getTempLowerBound(),
                request.getThicknessLevel(),
                request.getVisibility(),
                request.getClothUrl(),
                request.getBrand(),
                request.getCategoryId(),
                imageUrl
        );

        // ES 동기화
        asyncUpdatedClothFromES(existingCloth);
    }

    @Override
    @Transactional
    public void deleteClothById(Long clothId){
        historyClothRepository.deleteAllByClothId(clothId);
        clothFolderRepositoryService.deleteAllByClothId(clothId);
        clothImageRepository.deleteByClothId(clothId);
        clothRepository.deleteById(clothId);

        asyncDeletedClothFromES(clothId);
    }

    // 비동기 방식으로 Elasticsearch 수정 요청
    @Override
    public void asyncUpdatedClothFromES(Cloth cloth) {
        try {
            searchRepositoryService.updateClothDataToElasticsearch(cloth);
        } catch (IOException e) {
            searchRepositoryService.saveFailedUpdateES(cloth,FAILED_ES_UPDATE_SYNC_CLOTH_KEY); // 실패한 Cloth 저장 후 재시도 가능하도록 처리
            throw new SearchException(ErrorStatus.ELASTIC_SEARCH_SYNC_FAULT);
        }
    }

    // 비동기 방식으로 Elasticsearch 삭제 요청
    @Override
    public void asyncDeletedClothFromES(Long clothId) {
        try {
            searchRepositoryService.deleteClothByIdFromElasticsearch(clothId);
        } catch (IOException e) {
            searchRepositoryService.saveFailedDeletionES(clothId,FAILED_ES_DELETE_SYNC_CLOTH_KEY); // 실패한 ID 저장 후 재시도 가능하도록 처리
            throw new SearchException(ErrorStatus.ELASTIC_SEARCH_DELETE_FAULT);
        }
    }

    private List<Cloth> findBySmartSummaryFilters(
            @Param("type") SummaryFrequency type,
            @Param("memberId") Long memberId,
            @Param("categoryId") Long categoryId
    ){
        return switch (type) {
            case FREQUENT -> clothRepository.findMostFrequentClothList(memberId,categoryId);
            case INFREQUENT -> clothRepository.findLeastFrequentClothList(memberId,categoryId);
        };
    }
}
