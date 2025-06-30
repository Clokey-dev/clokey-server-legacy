package com.clokey.server.domain.search.application;

import com.clokey.server.domain.cloth.domain.repository.ClothImageRepository;
import com.clokey.server.domain.cloth.domain.repository.ClothRepository;
import com.clokey.server.domain.history.domain.repository.HashtagHistoryRepository;
import com.clokey.server.domain.history.domain.repository.HistoryClothRepository;
import com.clokey.server.domain.history.domain.repository.HistoryImageRepository;
import com.clokey.server.domain.history.domain.repository.HistoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.DeleteByQueryResponse;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import com.clokey.server.domain.cloth.domain.document.ClothDocument;
import com.clokey.server.domain.cloth.domain.entity.Cloth;
import com.clokey.server.domain.history.domain.document.HistoryDocument;
import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.history.domain.entity.HistoryImage;
import com.clokey.server.domain.member.application.MemberRepositoryService;
import com.clokey.server.domain.member.domain.document.MemberDocument;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.search.exception.SearchException;
import com.clokey.server.global.error.code.status.ErrorStatus;

@Service
@RequiredArgsConstructor
public class SearchRepositoryServiceImpl implements SearchRepositoryService {

    private final HistoryImageRepository historyImageRepository;
    private final HistoryRepository historyRepository;
    private final ElasticsearchClient elasticsearchClient;

    private final ClothRepository clothRepository;
    private final ClothImageRepository clothImageRepository;
    private static final String CLOTH_INDEX_NAME = "cloth";

    private final MemberRepositoryService memberRepositoryService;
    private static final String MEMBER_INDEX_NAME = "user";

    private final HashtagHistoryRepository hashtagHistoryRepository;
    private final HistoryClothRepository historyClothRepository;
    private static final String HISTORY_INDEX_NAME = "history";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String FAILED_ES_UPDATE_SYNC_CLOTH_KEY = "failed_es_update_sync_cloth";
    private static final String FAILED_ES_DELETE_SYNC_CLOTH_KEY = "failed_es_delete_sync_cloth";
    private static final String FAILED_ES_UPDATE_SYNC_HISTORY_KEY = "failed_es_update_sync_history";
    private static final String FAILED_ES_DELETE_SYNC_HISTORY_KEY = "failed_es_delete_sync_history";
    private static final String FAILED_ES_UPDATE_SYNC_USER_KEY = "failed_es_update_sync_user";
    private static final String FAILED_ES_DELETE_SYNC_USER_KEY = "failed_es_delete_sync_user";



    /****************************************Save For Retry Sync****************************************/

    // Update 실패 데이터 저장
    public void saveFailedUpdateES(Object object, String option) {
        String jsonData = convertObjectToJson(object);  // 객체를 JSON 문자열로 변환
        switch (option) {
            case FAILED_ES_UPDATE_SYNC_CLOTH_KEY -> redisTemplate.opsForList().rightPush(FAILED_ES_UPDATE_SYNC_CLOTH_KEY, jsonData);
            case FAILED_ES_UPDATE_SYNC_HISTORY_KEY -> redisTemplate.opsForList().rightPush(FAILED_ES_UPDATE_SYNC_HISTORY_KEY, jsonData);
            case FAILED_ES_UPDATE_SYNC_USER_KEY -> redisTemplate.opsForList().rightPush(FAILED_ES_UPDATE_SYNC_USER_KEY, jsonData);
        }
    }

    // Delete 실패 데이터 저장
    public void saveFailedDeletionES(Long id, String option) {
        switch (option) {
            case FAILED_ES_DELETE_SYNC_CLOTH_KEY -> redisTemplate.opsForList().rightPush(FAILED_ES_DELETE_SYNC_CLOTH_KEY, String.valueOf(id));
            case FAILED_ES_DELETE_SYNC_HISTORY_KEY -> redisTemplate.opsForList().rightPush(FAILED_ES_DELETE_SYNC_HISTORY_KEY, String.valueOf(id));
            case FAILED_ES_DELETE_SYNC_USER_KEY -> redisTemplate.opsForList().rightPush(FAILED_ES_DELETE_SYNC_USER_KEY, String.valueOf(id));
        }
    }

    // 객체 → JSON 변환 (엔티티 클래스를 자동으로 처리)
    private String convertObjectToJson(Object object) {
        try {
            // ObjectMapper에서 직접 엔티티 클래스를 매핑하여 JSON으로 변환
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    /****************************************Cloth Sync****************************************/

    // 단일 옷 데이터를 Elasticsearch로 저장하는 메서드
    @Override
    public void updateClothDataToElasticsearch(Cloth cloth) throws IOException {

        String imageUrl = clothImageRepository.findByClothId(cloth.getId()).getImageUrl();

        BulkOperation bulkOperation = BulkOperation.of(op -> op
                .index(IndexOperation.of(idx -> idx
                        .index(CLOTH_INDEX_NAME)
                        .id(cloth.getId().toString())
                        .document(ClothDocument.builder()
                                .id(cloth.getId())
                                .name(cloth.getName())
                                .brand(cloth.getBrand())
                                .imageUrl(imageUrl)
                                .wearNum(cloth.getWearNum())
                                .memberId(cloth.getMember().getId())
                                .visibility(cloth.getVisibility().toString())
                                .build())
                )));

        BulkResponse bulkResponse = elasticsearchClient.bulk(b -> b
                .index(CLOTH_INDEX_NAME)
                .operations(List.of(bulkOperation))
        );

        if (bulkResponse.errors()) {

            System.err.println("Elasticsearch 단일 cloth 데이터 업데이트 중 오류 발생: " + bulkResponse.toString());

            throw new SearchException(ErrorStatus.ELASTIC_SEARCH_SYNC_FAULT);
        }
    }

    // 특정 옷 Elasticsearch에서 삭제하는 메서드
    @Override
    public void deleteClothByIdFromElasticsearch(Long clothId) throws IOException {

        DeleteResponse deleteResponse = elasticsearchClient.delete(d -> d
                .index(CLOTH_INDEX_NAME)
                .id(clothId.toString())
        );

        if (!deleteResponse.result().equals(Result.Deleted)) {
            System.err.println("Elasticsearch에서 clothId: " + clothId + " 에 해당하는 데이터를 찾을 수 없습니다.");

            throw new SearchException(ErrorStatus.ELASTIC_SEARCH_DELETE_FAULT);
        }
    }

    // JPA에서 모든 Cloth 데이터 가져와서 Elasticsearch로 저장하는 메서드
    @Override
    public void syncAllClothesDataToElasticsearch() throws IOException {
        List<Cloth> clothList = clothRepository.findAll();

        List<BulkOperation> bulkOperations = clothList.stream()
                .map(cloth -> BulkOperation.of(op -> op
                        .index(IndexOperation.of(idx -> idx
                                .index(CLOTH_INDEX_NAME)
                                .id(cloth.getId().toString())
                                .document(ClothDocument.builder()
                                        .id(cloth.getId())
                                        .name(cloth.getName())
                                        .brand(cloth.getBrand())
                                        .imageUrl(cloth.getImage() != null ? cloth.getImage().getImageUrl() : null)
                                        .wearNum(cloth.getWearNum())
                                        .memberId(cloth.getMember().getId())
                                        .visibility(cloth.getVisibility().toString())
                                        .build())
                        ))))
                .collect(Collectors.toList());

        if (!bulkOperations.isEmpty()) {
            BulkResponse bulkResponse = elasticsearchClient.bulk(b -> b
                    .index(CLOTH_INDEX_NAME)
                    .operations(bulkOperations)
            );

            if (bulkResponse.errors()) {
                System.err.println("Elasticsearch cloth 동기화 중 오류 발생: " + bulkResponse.toString());

                throw new SearchException(ErrorStatus.ELASTIC_SEARCH_SYNC_FAULT);
            }
        }
    }

    /****************************************History Sync****************************************/

    // 단일 기록 데이터를 Elasticsearch로 저장하는 메서드
    @Override
    public void updateHistoryDataToElasticsearch(History history) throws IOException {

        List<String> hashtagNames = hashtagHistoryRepository.findHashtagNamesByHistoryId(history.getId());

        List<Cloth> clothes = historyClothRepository.findAllClothsWithCategoryByHistoryId(history.getId());

        List<String> categoryNames = clothes.stream()
                .map(cloth -> cloth.getCategory().getName())
                .distinct()
                .collect(Collectors.toList());

        String imageUrl = historyImageRepository.findByHistory_Id(history.getId()).stream()
                .sorted(Comparator.comparing(HistoryImage::getCreatedAt))
                .map(HistoryImage::getImageUrl)
                .findFirst()
                .orElse(null);

        BulkOperation bulkOperation = BulkOperation.of(op -> op
                .index(IndexOperation.of(idx -> idx
                        .index(HISTORY_INDEX_NAME)
                        .id(history.getId().toString())
                        .document(HistoryDocument.builder()
                                .id(history.getId())
                                .hashtagNames(hashtagNames)
                                .categoryNames(categoryNames)
                                .imageUrl(imageUrl)
                                .memberId(history.getMember().getId())
                                .memberVisibility(history.getMember().getVisibility().toString())
                                .historyVisibility(history.getVisibility().toString())
                                .build())
                )));

        BulkResponse bulkResponse = elasticsearchClient.bulk(b -> b
                .index(HISTORY_INDEX_NAME)
                .operations(List.of(bulkOperation))
        );

        if (bulkResponse.errors()) {
            System.err.println("Elasticsearch 단일 history 데이터 업데이트 중 오류 발생: " + bulkResponse.toString());

            throw new SearchException(ErrorStatus.ELASTIC_SEARCH_SYNC_FAULT);
        }
    }

    // 특정 옷 Elasticsearch에서 삭제하는 메서드
    @Override
    public void deleteHistoryByIdFromElasticsearch(Long historyId) throws IOException {

        DeleteResponse deleteResponse = elasticsearchClient.delete(d -> d
                .index(HISTORY_INDEX_NAME)
                .id(historyId.toString())
        );

        if (!deleteResponse.result().equals(Result.Deleted)) {
            System.err.println("Elasticsearch에서 historyId: " + historyId + " 에 해당하는 데이터를 찾을 수 없습니다.");

            throw new SearchException(ErrorStatus.ELASTIC_SEARCH_DELETE_FAULT);
        }
    }

    // JPA에서 모든 History 데이터 가져와서 Elasticsearch로 저장하는 메서드
    @Override
    public void syncAllHistoriesDataToElasticsearch() throws IOException {
        List<History> historyList = historyRepository.findAllWithMember();

        List<BulkOperation> bulkOperations = historyList.stream()
                .map(history -> {

                    List<String> hashtagNames = hashtagHistoryRepository.findHashtagNamesByHistoryId(history.getId());

                    List<Cloth> clothes = historyClothRepository.findAllClothsWithCategoryByHistoryId(history.getId());

                    List<String> categoryNames = clothes.stream()
                            .map(cloth -> cloth.getCategory().getName())
                            .distinct()
                            .collect(Collectors.toList());

                    String imageUrl = historyImageRepository.findByHistory_Id(history.getId()).stream()
                            .sorted(Comparator.comparing(HistoryImage::getCreatedAt))
                            .map(HistoryImage::getImageUrl)
                            .findFirst()
                            .orElse(null);

                    return BulkOperation.of(op -> op
                            .index(IndexOperation.of(idx -> idx
                                    .index(HISTORY_INDEX_NAME)
                                    .id(history.getId().toString())
                                    .document(HistoryDocument.builder()
                                            .id(history.getId())
                                            .hashtagNames(hashtagNames)
                                            .categoryNames(categoryNames)
                                            .imageUrl(imageUrl)
                                            .memberId(history.getMember().getId())
                                            .memberVisibility(history.getMember().getVisibility().toString())
                                            .historyVisibility(history.getVisibility().toString())
                                            .build())
                            )));
                })
                .collect(Collectors.toList());

        if (!bulkOperations.isEmpty()) {
            BulkResponse bulkResponse = elasticsearchClient.bulk(b -> b
                    .index(HISTORY_INDEX_NAME)
                    .operations(bulkOperations)
            );

            if (bulkResponse.errors()) {
                System.err.println("Elasticsearch history 동기화 중 오류 발생: " + bulkResponse.toString());

                throw new SearchException(ErrorStatus.ELASTIC_SEARCH_SYNC_FAULT);
            }
        }
    }

    /****************************************Member Sync****************************************/

    // 단일 유저 데이터를 Elasticsearch로 저장하는 메서드
    @Override
    public void updateMemberDataToElasticsearch(Member member) throws IOException {
        BulkOperation bulkOperation = BulkOperation.of(op -> op
                .index(IndexOperation.of(idx -> idx
                        .index(MEMBER_INDEX_NAME)
                        .id(member.getId().toString())
                        .document(MemberDocument.builder()
                                .id(member.getId())
                                .nickname(member.getNickname())
                                .clokeyId(member.getClokeyId())
                                .profileUrl(member.getProfileImageUrl())
                                .build())
                )));

        BulkResponse bulkResponse = elasticsearchClient.bulk(b -> b
                .index(MEMBER_INDEX_NAME)
                .operations(List.of(bulkOperation))
        );

        if (bulkResponse.errors()) {
            System.err.println("Elasticsearch 단일 member 데이터 업데이트 중 오류 발생: " + bulkResponse.toString());

            throw new SearchException(ErrorStatus.ELASTIC_SEARCH_SYNC_FAULT);
        }
    }

    // 특정 memberId를 가진 멤버의 Elasticsearch의 유저, 옷, 기록 데이터 삭제하는 메서드
    @Override
    public void deleteMemberAndClothesAndHistoriesByMemberIdFromElasticsearch(Long memberId) throws IOException {

        DeleteResponse deleteMemberResponse = elasticsearchClient.delete(d -> d
                .index(MEMBER_INDEX_NAME)
                .id(memberId.toString())
        );

        DeleteByQueryResponse deleteClothesResponse = elasticsearchClient.deleteByQuery(d -> d
                .index(CLOTH_INDEX_NAME)
                .query(q -> q
                        .term(t -> t.field("memberId").value(memberId))
                )
        );

        DeleteByQueryResponse deleteHistoriesResponse = elasticsearchClient.deleteByQuery(d -> d
                .index(HISTORY_INDEX_NAME)
                .query(q -> q
                        .term(t -> t.field("memberId").value(memberId))
                )
        );

        if (!deleteMemberResponse.result().equals(Result.Deleted)) {
            System.err.println("Elasticsearch에서 memberId: " + memberId + "을 memberId로 가지는 멤버에 해당하는 데이터를 찾을 수 없습니다.");

            throw new SearchException(ErrorStatus.ELASTIC_SEARCH_DELETE_FAULT);
        }
        if (deleteClothesResponse.deleted() == 0) {
            System.err.println("Elasticsearch에서 memberId: " + memberId + "유저가 가지는 옷에 해당하는 데이터를 찾을 수 없습니다.");
        }
        if (deleteHistoriesResponse.deleted() == 0) {
            System.err.println("Elasticsearch에서 Id: " + memberId + "유저가 가지는 기록에 해당하는 데이터를 찾을 수 없습니다.");
        }
    }

    // JPA에서 모든 Member 데이터 가져와서 Elasticsearch로 저장하는 메서드
    @Override
    public void syncAllMembersDataToElasticsearch() throws IOException {
        List<Member> memberList = memberRepositoryService.findAll();

        List<BulkOperation> bulkOperations = memberList.stream()
                .map(member -> BulkOperation.of(op -> op
                        .index(IndexOperation.of(idx -> idx
                                .index(MEMBER_INDEX_NAME)
                                .id(member.getId().toString())
                                .document(MemberDocument.builder()
                                        .id(member.getId())
                                        .nickname(member.getNickname())
                                        .clokeyId(member.getClokeyId())
                                        .profileUrl(member.getProfileImageUrl())
                                        .build())
                        ))))
                .collect(Collectors.toList());

        if (!bulkOperations.isEmpty()) {
            BulkResponse bulkResponse = elasticsearchClient.bulk(b -> b
                    .index(MEMBER_INDEX_NAME)
                    .operations(bulkOperations)
            );

            if (bulkResponse.errors()) {
                System.err.println("Elasticsearch 동기화 중 오류 발생: " + bulkResponse.toString());

                throw new SearchException(ErrorStatus.ELASTIC_SEARCH_SYNC_FAULT);
            }
        }
    }

}
