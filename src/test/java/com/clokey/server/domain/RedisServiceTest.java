package com.clokey.server.domain;

import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.clokey.server.global.infra.redis.RedisService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
@SpringBootTest
class RedisServiceTest {

    private RedisService redisService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        // Mockito로 Mock 객체 생성
        MockitoAnnotations.openMocks(this);
        // RedisTemplate의 opsForValue() 메서드를 mock 객체로 설정
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // RedisService 생성
        redisService = new RedisService(redisTemplate);
    }

    @Test
    void testGetSessionData() {
        // 준비: Redis에서 "sessionData:user123" 값을 리턴한다고 설정
        String userId = "user123";
        String expectedSessionData = "sessionDataValue";
        when(valueOperations.get("sessionData:" + userId)).thenReturn(expectedSessionData);

        // 실행: getSessionData 메서드 호출
        String sessionData = redisService.getSessionData(userId);

        // 검증: 반환값이 예상한 세션 데이터와 일치하는지 확인
        assertEquals(expectedSessionData, sessionData);

        // 확인: Redis에서 "sessionData:user123" 키로 get()을 호출했는지 확인
        verify(valueOperations).get("sessionData:" + userId);
    }

    @Test
    void testGetSessionData_whenNotFound() {
        // 준비: Redis에서 "sessionData:user123" 값이 없다고 설정
        String userId = "user123";
        when(valueOperations.get("sessionData:" + userId)).thenReturn(null);

        // 실행: getSessionData 메서드 호출
        String sessionData = redisService.getSessionData(userId);

        // 검증: 값이 없으면 null이 반환되어야 함
        assertNull(sessionData);

        // 확인: Redis에서 "sessionData:user123" 키로 get()을 호출했는지 확인
        verify(valueOperations).get("sessionData:" + userId);
    }
}
