package com.clokey.server.domain.member.application;

import com.clokey.server.domain.history.domain.repository.*;
import com.clokey.server.domain.member.scheduler.InactiveUserCleanupTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.clokey.server.domain.cloth.application.ClothImageRepositoryService;
import com.clokey.server.domain.cloth.application.ClothRepositoryService;
import com.clokey.server.domain.folder.application.ClothFolderRepositoryService;
import com.clokey.server.domain.folder.application.FolderRepositoryService;
import com.clokey.server.domain.history.application.*;
import com.clokey.server.domain.history.exception.validator.HistoryAccessibleValidator;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.member.exception.MemberException;
import com.clokey.server.domain.model.entity.enums.MemberStatus;
import com.clokey.server.domain.model.entity.enums.SocialType;
import com.clokey.server.domain.notification.application.NotificationRepositoryService;
import com.clokey.server.domain.search.application.SearchRepositoryService;
import com.clokey.server.domain.search.exception.SearchException;
import com.clokey.server.domain.term.application.MemberTermRepositoryService;
import com.clokey.server.global.error.code.status.ErrorStatus;


@Slf4j
@RequiredArgsConstructor
@Service
public class UnlinkServiceImpl implements UnlinkService {

    private final MemberTermRepositoryService memberTermRepositoryService;

    private final FollowRepositoryService followRepositoryService;
    private final HistoryRepository historyRepository;
    private final MemberRepositoryService memberRepositoryService;
    private final MemberLikeRepositoryService memberLikeRepositoryService;
    private final HashtagHistoryRepository hashtagHistoryRepository;
    private final ClothRepositoryService clothRepositoryService;
    private final HistoryClothRepository historyClothRepository;
    public final HistoryAccessibleValidator historyAccessibleValidator;
    private final ClothImageRepositoryService clothImageRepositoryService;
    private final ClothFolderRepositoryService clothFolderRepositoryService;
    private final FolderRepositoryService folderRepositoryService;
    private final NotificationRepositoryService notificationRepositoryService;
    private final SearchRepositoryService searchRepositoryService;
    private final AuthService authService;
    private final HistoryImageRepository historyImageRepository;
    private final CommentRepository commentRepository;

    private static final String FAILED_ES_DELETE_SYNC_USER_KEY = "failed_es_delete_sync_user";

    @Value("${kakao.admin-key}")
    private String KAKAO_ADMIN_KEY;

    @Value("${apple.client-id}")
    private String APPLE_CLIENT_ID;


    @Transactional
    @Override
    public void unlink(Long userId) {
        Member member = memberRepositoryService.findMemberById(userId);

        checkActiveMember(member);

        if (member != null && SocialType.KAKAO == member.getSocialType()) {
            String kakaoId = member.getKakaoId();
            if (kakaoId != null) {
                kakaoUnlink(kakaoId);
            }
        } else if (member != null && SocialType.APPLE == member.getSocialType()) {
            log.info("🍏 애플 연동 해제 실행됨");
            String clientSecret = authService.createClientSecret();  // ✅ 새로 생성
            String refreshToken = member.getAppleRefreshToken();
            if (clientSecret != null && refreshToken != null) {
                appleUnlink(clientSecret, refreshToken);
            }
        }

        if (member != null) {
            member.updateToken(null, null);
            memberRepositoryService.saveMember(member);
            // 토큰 무효화 처리
        }

        member.updateStatus();
        member.updateInactiveDate(LocalDate.now());
        memberRepositoryService.saveMember(member);
    }

    public void kakaoUnlink(String kakaoId) {
        String url = "https://kapi.kakao.com/v1/user/unlink";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "KakaoAK " + KAKAO_ADMIN_KEY);

        try {
            Long kakaoUserId = Long.parseLong(kakaoId);
            String body = "target_id_type=user_id&target_id=" + kakaoUserId;

            HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("✅ 카카오 연동 해제 성공: {}", response.getBody());
            } else {
                log.warn("⚠️ 카카오 연동 해제 실패: HTTP {}", response.getStatusCode());
            }
        } catch (NumberFormatException e) {
            log.error("카카오 연동 해제 실패: kakaoId 변환 오류", e);
        }
    }

    public void appleUnlink(String clientSecret, String refreshToken) {

        String uriStr = "https://appleid.apple.com/auth/revoke";

        Map<String, String> params = new HashMap<>();
        params.put("client_secret", clientSecret); // 생성한 client_secret
        params.put("token", refreshToken); // 생성한 refresh_token
        params.put("client_id", APPLE_CLIENT_ID); // app bundle id

        try {
            HttpRequest getRequest = HttpRequest.newBuilder().uri(new URI(uriStr)).POST(authService.getParamsUrlEncoded(params)).headers("Content-Type", "application/x-www-form-urlencoded").build();

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpResponse<String> response = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

            // 응답 상태 코드와 본문 출력
            log.info("🍏 응답 상태 코드: {}", response.statusCode());
            log.info("🍏 애플 연동 해제 결과: {}", response.body());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Transactional
    public void deleteData(Long memberId) {
        Member member = memberRepositoryService.findMemberById(memberId);

        // ES 동기화
        asyncDeletedMemberFromES(memberId);

        LocalDate inactiveDate = member.getInactiveDate();
        if (inactiveDate == null || inactiveDate.isAfter(LocalDate.now().minusDays(15))) {
            log.info("삭제 대상이 아님: userId={}, inactiveDate={}", memberId, inactiveDate);
            return;
        }

        // 멤버텀 삭제
        memberTermRepositoryService.deleteByMemberId(memberId);

        //기록 삭제
        List<Long> historyIds = memberRepositoryService.findHistoryIdsByMemberId(memberId);

        commentRepository.deleteChildrenByHistoryIds(historyIds);
        commentRepository.deleteCommentsByHistoryIds(historyIds);
        historyClothRepository.deleteAllByHistoryIds(historyIds);
        hashtagHistoryRepository.deleteAllByHistoryIds(historyIds);
        memberLikeRepositoryService.deleteAllByHistoryIds(historyIds);
        historyImageRepository.deleteAllByHistoryIds(historyIds);
        historyRepository.deleteByHistoryIds(historyIds);


        // 팔로우 삭제
        followRepositoryService.deleteByMemberId(memberId);

        //옷 삭제
        List<Long> clothIds = memberRepositoryService.findClothIdsByMemberId(memberId);

        clothFolderRepositoryService.deleteAllByClothIds(clothIds);
        clothImageRepositoryService.deleteAllByClothIds(clothIds);
        clothRepositoryService.deleteByClothIds(clothIds);


        //폴더 삭제
        List<Long> folderIds = memberRepositoryService.findFolderIdsByMemberId(memberId);
        clothFolderRepositoryService.deleteAllByFolderIds(folderIds);
        folderRepositoryService.deleteByFolderIds(folderIds);


        //댓글 삭제
        List<Long> commentIds = memberRepositoryService.findCommentIdsByMemberId(memberId);

        commentRepository.deleteChildrenByCommentIds(commentIds);
        commentRepository.deleteCommentsByCommentIds(commentIds);


        //알람 삭제
        List<Long> notificationIds = memberRepositoryService.findNotificationIdsByMemberId(memberId);

        notificationRepositoryService.deleteByClokeyNotificationIds(notificationIds);

        //좋아요 삭제
        memberLikeRepositoryService.deleteAllByMemberId(memberId);

        memberRepositoryService.deleteMemberById(memberId);  // 최종적으로 회원 삭제

        log.info("회원 및 관련 데이터 삭제 완료: userId={}", memberId);
    }

    // 비동기 방식으로 Elasticsearch 삭제 요청
    public void asyncDeletedMemberFromES(Long memberId) {
        try {
            searchRepositoryService.deleteMemberAndClothesAndHistoriesByMemberIdFromElasticsearch(memberId);
        } catch (IOException e) {
            searchRepositoryService.saveFailedDeletionES(memberId,FAILED_ES_DELETE_SYNC_USER_KEY); // 실패한 ID 저장 후 재시도 가능하도록 처리
            throw new SearchException(ErrorStatus.ELASTIC_SEARCH_DELETE_FAULT);
        }
    }

    void checkActiveMember(Member member) {
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new MemberException(ErrorStatus.INACTIVE_MEMBER);
        }
    }

}
