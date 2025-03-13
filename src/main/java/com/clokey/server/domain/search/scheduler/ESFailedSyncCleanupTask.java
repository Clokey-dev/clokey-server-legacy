package com.clokey.server.domain.search.scheduler;

import com.clokey.server.domain.cloth.domain.entity.Cloth;
import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.search.application.SearchRepositoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class ESFailedSyncCleanupTask {

    private final SearchRepositoryService searchRepositoryService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String FAILED_ES_UPDATE_SYNC_CLOTH_KEY = "failed_es_update_sync_cloth";
    private static final String FAILED_ES_DELETE_SYNC_CLOTH_KEY = "failed_es_delete_sync_cloth";
    private static final String FAILED_ES_UPDATE_SYNC_HISTORY_KEY = "failed_es_update_sync_history";
    private static final String FAILED_ES_DELETE_SYNC_HISTORY_KEY = "failed_es_delete_sync_history";
    private static final String FAILED_ES_UPDATE_SYNC_USER_KEY = "failed_es_update_sync_user";
    private static final String FAILED_ES_DELETE_SYNC_USER_KEY = "failed_es_delete_sync_user";

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void retryFailedEsUpdateSync() {
        processFailedUpdate(FAILED_ES_UPDATE_SYNC_CLOTH_KEY, entity -> {
            try {
                searchRepositoryService.updateClothDataToElasticsearch((Cloth) entity);
            } catch (IOException e) {
                redisTemplate.opsForList().rightPush(FAILED_ES_UPDATE_SYNC_CLOTH_KEY, convertObjectToJson(entity)); // 실패 시 Redis에 다시 저장
                throw new RuntimeException(e);
            }
        }, Cloth.class);

        processFailedUpdate(FAILED_ES_UPDATE_SYNC_HISTORY_KEY, entity -> {
            try {
                searchRepositoryService.updateHistoryDataToElasticsearch((History) entity);
            } catch (IOException e) {
                redisTemplate.opsForList().rightPush(FAILED_ES_UPDATE_SYNC_HISTORY_KEY, convertObjectToJson(entity)); // 실패 시 Redis에 다시 저장
                throw new RuntimeException(e);
            }
        }, History.class);

        processFailedUpdate(FAILED_ES_UPDATE_SYNC_USER_KEY, entity -> {
            try {
                searchRepositoryService.updateMemberDataToElasticsearch((Member) entity);
            } catch (IOException e) {
                redisTemplate.opsForList().rightPush(FAILED_ES_UPDATE_SYNC_USER_KEY, convertObjectToJson(entity)); // 실패 시 Redis에 다시 저장
                throw new RuntimeException(e);
            }
        }, Member.class);
    }

    // 공통 처리 메서드 - Update
    private void processFailedUpdate(String redisKey, Consumer<Object> updateMethod, Class<?> entityClass) {
        while (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            String jsonData = redisTemplate.opsForList().leftPop(redisKey);
            if (jsonData != null) {
                try {
                    // JSON을 해당 엔티티 클래스로 변환
                    Object entity = convertJsonToObject(jsonData, entityClass);
                    updateMethod.accept(entity);
                } catch (IllegalArgumentException e) {
                    redisTemplate.opsForList().rightPush(redisKey, jsonData); // 실패 시 Redis에 다시 저장
                }
            }
        }
    }

    // JSON → 객체 변환 (엔티티 클래스로 변환)
    private <T> T convertJsonToObject(String jsonData, Class<T> clazz) {
        try {
            // 지정된 엔티티 클래스로 JSON을 변환
            return new ObjectMapper().readValue(jsonData, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert JSON to object", e);
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

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void retryFailedEsDeleteSync() {
        processFailedDelete(FAILED_ES_DELETE_SYNC_CLOTH_KEY, id -> {
            try {
                searchRepositoryService.deleteClothByIdFromElasticsearch(id);
            } catch (IOException e) {
                redisTemplate.opsForList().rightPush(FAILED_ES_DELETE_SYNC_CLOTH_KEY, String.valueOf(id)); // 실패 시 Redis에 다시 저장
                throw new RuntimeException(e);
            }
        });

        processFailedDelete(FAILED_ES_DELETE_SYNC_HISTORY_KEY, id -> {
            try {
                searchRepositoryService.deleteHistoryByIdFromElasticsearch(id);
            } catch (IOException e) {
                redisTemplate.opsForList().rightPush(FAILED_ES_DELETE_SYNC_HISTORY_KEY, String.valueOf(id)); // 실패 시 Redis에 다시 저장
                throw new RuntimeException(e);
            }
        });

        processFailedDelete(FAILED_ES_DELETE_SYNC_USER_KEY, id -> {
            try {
                searchRepositoryService.deleteMemberAndClothesAndHistoriesByMemberIdFromElasticsearch(id);
            } catch (IOException e) {
                redisTemplate.opsForList().rightPush(FAILED_ES_DELETE_SYNC_USER_KEY, String.valueOf(id)); // 실패 시 Redis에 다시 저장
                throw new RuntimeException(e);
            }
        });
    }

    // 공통 처리 메서드 - Delete
    private void processFailedDelete(String redisKey, Consumer<Long> deleteMethod) {
        while (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            String idStr = redisTemplate.opsForList().leftPop(redisKey);
            if (idStr != null) {
                try {
                    deleteMethod.accept(Long.valueOf(idStr));
                } catch (IllegalArgumentException e) {
                    redisTemplate.opsForList().rightPush(redisKey, idStr); // 실패 시 Redis에 다시 저장
                }
            }
        }
    }
}
