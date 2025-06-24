package com.clokey.server.domain.history.application;

import com.clokey.server.domain.cloth.domain.repository.ClothRepository;
import com.clokey.server.domain.history.domain.repository.*;
import com.clokey.server.domain.history.dto.projection.*;
import com.clokey.server.domain.member.domain.repository.FollowRepository;
import com.clokey.server.domain.member.domain.repository.MemberRepository;
import com.clokey.server.domain.member.dto.projection.LikedMemberProjectionDTO;
import com.clokey.server.domain.member.exception.MemberException;
import com.clokey.server.global.infra.s3.S3ImageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.cloth.domain.entity.Cloth;
import com.clokey.server.domain.cloth.exception.validator.ClothAccessibleValidator;
import com.clokey.server.domain.history.converter.HistoryConverter;
import com.clokey.server.domain.history.domain.entity.*;
import com.clokey.server.domain.history.dto.HistoryRequestDTO;
import com.clokey.server.domain.history.dto.HistoryResponseDTO;
import com.clokey.server.domain.history.exception.HistoryException;
import com.clokey.server.domain.history.exception.validator.HistoryAccessibleValidator;
import com.clokey.server.domain.history.exception.validator.HistoryLikedValidator;
import com.clokey.server.domain.member.application.MemberRepositoryService;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.model.entity.enums.Visibility;
import com.clokey.server.domain.search.application.SearchRepositoryService;
import com.clokey.server.domain.search.exception.SearchException;
import com.clokey.server.global.error.code.status.ErrorStatus;

import static com.clokey.server.domain.history.exception.validator.HashtagConditionValidator.MAXIMUM_HASHTAGS;

@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final HistoryLikedValidator historyLikedValidator;
    private final MemberRepositoryService memberRepositoryService;
    private final ClothRepository clothRepository;
    private final ClothAccessibleValidator clothAccessibleValidator;
    private final HistoryAccessibleValidator historyAccessibleValidator;
    private final SearchRepositoryService searchRepositoryService;
    private final HistoryRepository historyRepository;
    private final HistoryImageRepository historyImageRepository;
    private final MemberRepository memberRepository;
    private final S3ImageService s3ImageService;
    private final HistoryClothRepository historyClothRepository;
    private final HashtagHistoryRepository hashtagHistoryRepository;
    private final HashtagRepository hashtagRepository;
    private final CommentRepository commentRepository;
    private final MemberLikeRepository memberLikeRepository;
    private final FollowRepository followRepository;

    private static final String FAILED_ES_UPDATE_SYNC_HISTORY_KEY = "failed_es_update_sync_history";
    private static final String FAILED_ES_DELETE_SYNC_HISTORY_KEY = "failed_es_delete_sync_history";

    @Override
    @Transactional
    public HistoryResponseDTO.LikeResult changeLike(Long memberId, Long historyId, boolean isLiked) {

        historyLikedValidator.validateIsLiked(historyId, memberId, isLiked);
        History history = historyRepository.findById(historyId).orElseThrow(()-> new HistoryException(ErrorStatus.NO_SUCH_HISTORY));

        if (isLiked) {
            history.decrementLikes();
            memberLikeRepository.deleteByMemberIdAndHistoryId(memberId, historyId);
        } else {
            history.incrementLikes();
            MemberLike memberLike = MemberLike.builder()
                    .history(history)
                    .member(memberRepositoryService.findMemberById(memberId))
                    .build();
            memberLikeRepository.save(memberLike);
        }

        return HistoryConverter.toLikeResult(history, isLiked);
    }

    @Override
    @Transactional
    public HistoryResponseDTO.CommentWriteResult writeComment(Long historyId, Long parentCommentId, Long memberId, String content) {

        validateParentCommentHistory(historyId, parentCommentId);

        History history = historyRepository.findById(historyId).orElseThrow(()-> new HistoryException(ErrorStatus.NO_SUCH_HISTORY));

        Member member = memberRepositoryService.findMemberById(memberId);

        Comment parentComment = null;
        if (parentCommentId != null) {
            parentComment = commentRepository.findById(parentCommentId).orElseThrow(()->new HistoryException(ErrorStatus.NO_SUCH_COMMENT));;
        }

        Comment comment = Comment.builder()
                .content(content)
                .comment(parentComment)
                .history(history)
                .member(member)
                .build();

        Comment savedComment = commentRepository.save(comment);

        return HistoryConverter.toCommentWriteResult(savedComment);
    }

    private void validateParentCommentHistory(Long historyId, Long parentCommentId) {
        if (parentCommentId == null) {
            return;
        }

        Long parentHistoryId = commentRepository.findById(parentCommentId)
                .orElseThrow(()->new HistoryException(ErrorStatus.NO_SUCH_COMMENT))
                .getHistory().getId();

        if (!parentHistoryId.equals(historyId)) {
            throw new HistoryException(ErrorStatus.PARENT_COMMENT_HISTORY_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public HistoryResponseDTO.DailyHistoryResult getDaily(Long historyId, Long memberId) {
        historyAccessibleValidator.validateHistoryAccessOfMember(historyId, memberId);

        History history = historyRepository.findByIdWithWriter(historyId)
                .orElseThrow(()-> new HistoryException(ErrorStatus.NO_SUCH_HISTORY));

        List<String> imageUrl = historyImageRepository.getImageUrlsByHistoryIdOrderByCreatedAtAsc(historyId);
        List<String> hashtags = hashtagHistoryRepository.findHashtagNamesByHistoryId(historyId);
        boolean isLiked = memberLikeRepository.existsByMemberIdAndHistoryId(memberId, historyId);
        Long commentCount = commentRepository.countByHistoryId(historyId);

        Member historyWriter = history.getMember();
        List<DailyHistoryClothProjectionDTO> dailyHistoryClothProjectionDTOS = historyRepository.findClothesByHistoryId(historyId);

        if (memberId.equals(historyWriter.getId())){
            return HistoryConverter.toDayViewResult(history,historyWriter, imageUrl, hashtags, isLiked, dailyHistoryClothProjectionDTOS, commentCount);
        } else {
            dailyHistoryClothProjectionDTOS = dailyHistoryClothProjectionDTOS.stream()
                    .filter(cloth -> cloth.getVisibility() == Visibility.PUBLIC)
                    .collect(Collectors.toList());
            return HistoryConverter.toDayViewResult(history,historyWriter, imageUrl, hashtags, isLiked, dailyHistoryClothProjectionDTOS, commentCount);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public HistoryResponseDTO.HistoryCommentResult getComments(Long historyId, int page) {
        List<HistoryCommentProjectionDTO> commentsDTO = historyRepository.findFlatCommentsByHistoryId(historyId,page,20);
        int totalRootCount = commentRepository.countActiveRootComments(historyId);
        return HistoryConverter.toHistoryCommentResult(commentsDTO,page,20,totalRootCount);
    }

    @Override
    @Transactional(readOnly = true)
    public HistoryResponseDTO.MonthViewResult getMonthlyHistories(Long myMemberId, String clokeyId, String month) {

        //Clokey ID를 제공하지 않았다면 자기 자신의 기록 확인으로 전부 반환.
        if (clokeyId == null) {
            List<HistoryProjectionDTO> histories = historyRepository.getMonthlyHistoriesByMemberAndYearMonth(myMemberId, month);

            List<String> firstImageUrlsOfHistory = findFirstImagesByHistoryIds(
                    histories.stream()
                            .map(HistoryProjectionDTO::getId)
                            .toList()
            );

            String nickName = memberRepository.findById(myMemberId)
                    .orElseThrow(() -> new MemberException(ErrorStatus.NO_SUCH_MEMBER))
                    .getNickname();

            return HistoryConverter.toMonthViewResult(myMemberId, nickName, histories, firstImageUrlsOfHistory);
        }

        Member member = memberRepository.findByClokeyId(clokeyId)
                .orElseThrow(() -> new MemberException(ErrorStatus.NO_SUCH_MEMBER));

        historyAccessibleValidator.validateMemberAccessOfMember(member.getId(), myMemberId);

        List<HistoryProjectionDTO> histories = historyRepository.getMonthlyHistoriesByMemberAndYearMonth(member.getId(), month);
        List<String> firstImageUrlsOfHistory = findFirstImagesByHistoryIds(
                histories.stream()
                        .map(HistoryProjectionDTO::getId)
                        .toList()
        );

        for (int i = 0; i < histories.size(); i++) {
            HistoryProjectionDTO history = histories.get(i);

            if (history.getVisibility().equals(Visibility.PRIVATE)) {
                firstImageUrlsOfHistory.set(i, "비공개입니다");
            }

        }
        return HistoryConverter.toMonthViewResult(member.getId(), member.getNickname(), histories, firstImageUrlsOfHistory);
    }

    @Override
    @Transactional
    public HistoryResponseDTO.HistoryCreateResult createHistory(HistoryRequestDTO.HistoryCreate historyCreateRequest, Long memberId, List<MultipartFile> imageFiles) {

        //모든 옷이 나의 옷이 맞는지 검증합니다.
        clothAccessibleValidator.validateClothOfMember(historyCreateRequest.getClothes(), memberId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        boolean historyExist = historyRepository.existsByHistoryDateAndMember_Id(LocalDate.parse(historyCreateRequest.getDate(), formatter), memberId);

        if (historyExist) {
            return updateHistory(historyCreateRequest, memberId, historyRepository.findByHistoryDateAndMember_Id(LocalDate.parse(historyCreateRequest.getDate()), memberId).orElseThrow(()->new HistoryException(ErrorStatus.NO_SUCH_HISTORY)).getId(), imageFiles);
        } else {

            History history = historyRepository.save(HistoryConverter.toHistory(historyCreateRequest, memberRepository.findById(memberId).orElseThrow(()-> new MemberException(ErrorStatus.NO_SUCH_MEMBER))));

            //사진 저장
            List<String> uploadedImageUrls = s3ImageService.uploadAll(imageFiles);
            List<HistoryImage> historyImages = uploadedImageUrls.stream()
                    .map(url -> HistoryImage.builder()
                            .history(history)
                            .imageUrl(url)
                            .build())
                    .collect(Collectors.toList());
            historyImageRepository.saveAll(historyImages);

            List<Cloth> cloths = clothRepository.findAllById(historyCreateRequest.getClothes());
            List<HistoryCloth> historyCloths = cloths.stream()
                    .map(cloth -> {
                        cloth.increaseWearNum();
                        return HistoryCloth.builder()
                                .history(history)
                                .cloth(cloth)
                                .build();
                    }).toList();
            historyClothRepository.saveAll(historyCloths);

            historyCreateRequest.getHashtags()
                    .forEach(hashtagNames -> {
                        //존재하는 해시태그라면 매핑 테이블에 추가
                        //아니라면 새로운 해시태그를 만들고 매핑 테이블에 추가
                        if (hashtagRepository.existsByName(hashtagNames)) {
                            hashtagHistoryRepository.save(HashtagHistory.builder()
                                    .history(history)
                                    .hashtag(hashtagRepository.findByName(hashtagNames).orElseThrow(()-> new HistoryException(ErrorStatus.NO_SUCH_HASHTAG_NAME)))
                                    .build()
                            );
                        } else {
                            Hashtag newHashtag = Hashtag.builder()
                                    .name(hashtagNames)
                                    .build();
                            hashtagRepository.save(newHashtag);

                            hashtagHistoryRepository.save(HashtagHistory.builder()
                                    .history(history)
                                    .hashtag(newHashtag)
                                    .build()
                            );
                        }
                    });

            // ES 동기화
            asyncUpdatedHistoryFromES(history);

            return HistoryConverter.toHistoryCreateResult(history);
        }
    }

    private HistoryResponseDTO.HistoryCreateResult updateHistory(HistoryRequestDTO.HistoryCreate historyUpdate, Long memberId, Long historyId, List<MultipartFile> images) {

        validateVisualizeBannedHistory(historyId,historyUpdate);

        historyAccessibleValidator.validateMyHistory(historyId, memberId);

        List<HistoryImage> historyImagesToDelete = historyImageRepository.findByHistory_Id(historyId);

        if (historyImagesToDelete != null && !historyImagesToDelete.isEmpty()) {
            s3ImageService.deleteAllFromS3(historyImagesToDelete.stream()
                    .map(HistoryImage::getImageUrl)
                    .toList());
            historyImageRepository.deleteAll(historyImagesToDelete);
        }


        History history = historyRepository.findById(historyId).orElseThrow(()-> new HistoryException(ErrorStatus.NO_SUCH_HISTORY));

        //사진 저장
        List<String> uploadedImageUrls = s3ImageService.uploadAll(images);
        List<HistoryImage> historyImages = uploadedImageUrls.stream()
                .map(url -> HistoryImage.builder()
                        .history(history)
                        .imageUrl(url)
                        .build())
                .collect(Collectors.toList());
        historyImageRepository.saveAll(historyImages);

        updateHistoryClothes(
                historyUpdate.getClothes(),
                historyClothRepository.findClothIdsByHistoryId(historyId),
                history);

        updateHistoryHashtags(
                historyUpdate.getHashtags(),
                hashtagHistoryRepository.findByHistory_Id(historyId).stream()
                        .map(hashtagHistory -> hashtagHistory.getHashtag().getName())
                        .toList(),
                history);

        history.updateHistory(historyUpdate.getContent(), historyUpdate.getVisibility());

        // ES 동기화
        asyncUpdatedHistoryFromES(history);

        return HistoryConverter.toHistoryCreateResult(history);
    }

    private void validateVisualizeBannedHistory(Long historyId, HistoryRequestDTO.HistoryCreate historyUpdate){
        boolean banned = historyRepository.findById(historyId)
                .orElseThrow(()-> new HistoryException(ErrorStatus.NO_SUCH_HISTORY))
                .isBanned();
        boolean changeToPublic = historyUpdate.getVisibility().equals(Visibility.PUBLIC);

        if(banned && changeToPublic){
            throw new HistoryException(ErrorStatus.BANNED_HISTORY_TO_PUBLIC);
        }
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long memberId) {
        validateMyComment(commentId, memberId);
        commentRepository.deleteChildren(commentId);
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public void updateComment(HistoryRequestDTO.UpdateComment updateCommentRequest, Long commentId, Long memberId) {
        validateMyComment(commentId, memberId);
        Comment commentToUpdate = commentRepository.findById(commentId).orElseThrow(()-> new HistoryException(ErrorStatus.NO_SUCH_COMMENT));
        commentToUpdate.updateContent(updateCommentRequest.getContent());
    }

    @Override
    @Transactional
    public void deleteHistory(Long historyId, Long memberId) {
        historyAccessibleValidator.validateMyHistory(historyId, memberId);

        commentRepository.deleteRepliesByHistoryId(historyId);
        commentRepository.deleteParentCommentsByHistoryId(historyId);

        //기록_옷 지우기
        List<Cloth> cloths = historyClothRepository.findAllClothsByHistoryId(historyId);
        cloths.forEach(Cloth::decreaseWearNum);
        historyClothRepository.deleteAllByHistoryId(historyId);

        //기록-해시태그 지우기
        hashtagHistoryRepository.deleteAllByHistoryId(historyId);

        //좋아요 기록 삭제
        memberLikeRepository.deleteByHistoryId(historyId);

        //기록 사진 삭제
        List<HistoryImage> historyImagesToDelete = historyImageRepository.findByHistory_Id(historyId);

        if (historyImagesToDelete != null && !historyImagesToDelete.isEmpty()) {
            s3ImageService.deleteAllFromS3(historyImagesToDelete.stream()
                    .map(HistoryImage::getImageUrl)
                    .toList());
            historyImageRepository.deleteAll(historyImagesToDelete);
        }

        //기록 삭제
        historyRepository.deleteById(historyId);

        // ES 삭제
        asyncDeletedHistoryFromES(historyId);
    }

    @Override
    @Transactional(readOnly = true)
    public HistoryResponseDTO.CheckMyHistoryResult checkIfHistoryIsMine(Long historyId, Long memberId) {

        return HistoryResponseDTO.CheckMyHistoryResult.builder()
                .isMyHistory(historyRepository.checkMyHistory(historyId,memberId))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public HistoryResponseDTO.LikedUserResults getLikedUsers(Long memberId, Long historyId) {

        historyAccessibleValidator.validateHistoryAccessOfMember(historyId, memberId);

        List<LikedMemberProjectionDTO> likedMembers = memberRepository.findLikedMemberDTOsByHistoryId(historyId);
        List<Boolean> followStatus = checkFollowingStatus(memberId, likedMembers.stream()
                .map(LikedMemberProjectionDTO::getMemberId)
                .toList());
        List<Boolean> isMySelf = likedMembers.stream()
                .map(likedMemberId -> likedMemberId.getMemberId().equals(memberId))
                .toList();

        return HistoryConverter.toLikedUserResult(likedMembers, followStatus,isMySelf);
    }

    private void validateMyComment(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(()->new HistoryException(ErrorStatus.NO_SUCH_COMMENT));;
        if (!comment.getMember().getId().equals(memberId)) {
            throw new HistoryException(ErrorStatus.NOT_MY_COMMENT);
        }
    }

    private void updateHistoryClothes(List<Long> updatedClothes, List<Long> savedClothes, History history) {

        //updateClothes에만 존재하는 것은 추가 대상
        List<Cloth> clothesToAdd = clothRepository.findAllById(
                updatedClothes.stream()
                        .filter(clothId -> !savedClothes.contains(clothId))
                        .toList());

        //반대는 삭제 대상
        List<Cloth> clothesToDelete = clothRepository.findAllById(savedClothes.stream()
                .filter(clothId -> !updatedClothes.contains(clothId))
                .toList());

        clothesToAdd.forEach(cloth -> {
            historyClothRepository.save(HistoryCloth.builder()
                    .history(history)
                    .cloth(cloth)
                    .build()
            );
            cloth.increaseWearNum();
        });

        clothesToDelete.forEach(cloth -> {
            historyClothRepository.deleteByHistoryAndCloth(history, cloth);
            cloth.decreaseWearNum();
        });
    }

    private void updateHistoryHashtags(List<String> updatedHashtags, List<String> savedHashtags, History history) {

        //존재하지 않는 해시태그는 만들어준다.
        updatedHashtags.forEach(hashtagName -> {
            if (!hashtagRepository.existsByName(hashtagName)) {
                Hashtag newHashtag = Hashtag.builder()
                        .name(hashtagName)
                        .build();
                hashtagRepository.save(newHashtag);
            }
        });


        List<Hashtag> hashtagToAdd = hashtagRepository.findHashtagsByNames(updatedHashtags.stream()
                .filter(hashtagNames -> !savedHashtags.contains(hashtagNames))
                .toList());

        List<Hashtag> hashtagToDelete = hashtagRepository.findHashtagsByNames(savedHashtags.stream()
                .filter(hashtagNames -> !updatedHashtags.contains(hashtagNames))
                .toList());

        if(savedHashtags.size()+hashtagToAdd.size()-hashtagToAdd.size() > MAXIMUM_HASHTAGS){
            throw new HistoryException(ErrorStatus.TOO_MANY_HASHTAGS);
        }

        hashtagToAdd.forEach(hashtag -> addHashtagHistory(hashtag, history));
        hashtagToDelete.forEach(hashtag -> hashtagHistoryRepository.deleteByHashtagAndHistory(hashtag, history));
    }

    private void addHashtagHistory(Hashtag hashtag, History history) {

        HashtagHistory hashtagHistory = HashtagHistory.builder()
                .hashtag(hashtag)
                .history(history)
                .build();

        hashtagHistoryRepository.save(hashtagHistory);
    }
    
    // 비동기 방식으로 Elasticsearch 수정 요청
    @Override
    public void asyncUpdatedHistoryFromES(History history) {
        try {
            searchRepositoryService.updateHistoryDataToElasticsearch(history);
        } catch (IOException e) {
            searchRepositoryService.saveFailedUpdateES(history,FAILED_ES_UPDATE_SYNC_HISTORY_KEY); // 실패한 History 저장 후 재시도 가능하도록 처리
            throw new SearchException(ErrorStatus.ELASTIC_SEARCH_SYNC_FAULT);
        }
    }

    // 비동기 방식으로 Elasticsearch 삭제 요청
    @Override
    public void asyncDeletedHistoryFromES(Long historyId) {
        try {
            searchRepositoryService.deleteHistoryByIdFromElasticsearch(historyId);
        } catch (IOException e) {
            searchRepositoryService.saveFailedDeletionES(historyId,FAILED_ES_DELETE_SYNC_HISTORY_KEY); // 실패한 ID 저장 후 재시도 가능하도록 처리
            throw new SearchException(ErrorStatus.ELASTIC_SEARCH_DELETE_FAULT);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public HistoryResponseDTO.HistoryLikedListResult getLikedHistories(Long memberId, int page) {
        Page<HistoryProjectionDTO> histories = historyRepository.findLikedHistoryAndAuthorIds(memberId, PageRequest.of(page, 15));
        List<String> firstImageUrls = findFirstImagesByHistoryIds(histories.stream()
                .map(HistoryProjectionDTO::getId)
                .toList());
        return HistoryConverter.toHistoryLikedListResult(histories, firstImageUrls, memberId);
    }

    private List<String> findFirstImagesByHistoryIds(List<Long> historyIds) {
        if (historyIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Object[]> rows = historyImageRepository.getFirstImageUrlsWithHistoryId(historyIds);

        Map<Long, String> imageUrlMap = rows.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> (String) row[1]
                ));

        return historyIds.stream()
                .map(imageUrlMap::get)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public HistoryResponseDTO.HistoryMyCommentListResult getMyComments(Long memberId, int page) {
        Page<Comment> commentsPage = commentRepository.findByMember_Id(memberId, PageRequest.of(page, 10));

        Map<Long, List<HistoryResponseDTO.MyCommentResult>> groupedComments = commentsPage.getContent().stream()
                .filter(comment -> comment.getHistory()!=null)
                .collect(Collectors.groupingBy(
                comment -> comment.getHistory().getId(),
                Collectors.mapping(HistoryConverter::toMyCommentResult, Collectors.toList())
        ));

        List<History> histories = commentsPage.getContent().stream()
                .map(Comment::getHistory)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<String> imageUrls = findFirstImagesByHistoryIds(histories.stream()
                .map(History::getId)
                .toList());

        Map<Long, String> imageUrlMap = IntStream.range(0, histories.size())
                .boxed()
                .collect(Collectors.toMap(
                        i -> histories.get(i).getId(),
                        imageUrls::get
                ));

        List<HistoryResponseDTO.HistoryMyCommentResult> historyMyCommentResults = histories.stream()
                .map(history -> {
                    List<HistoryResponseDTO.MyCommentResult> commentList = groupedComments.getOrDefault(history.getId(), List.of());
                    return HistoryConverter.toHistoryMyCommentResult(history, commentList, imageUrlMap);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return HistoryConverter.toHistoryMyCommentListResult(commentsPage, historyMyCommentResults);
    }

    private List<Boolean> checkFollowingStatus(Long followedId, List<Long> memberIds) {
        List<Object[]> results = followRepository.findFollowingStatusByMemberIds(followedId, memberIds);

        Map<Long, Boolean> statusMap = new LinkedHashMap<>();
        for (Object[] result : results) {
            statusMap.put((Long) result[0], (Boolean) result[1]);
        }

        return memberIds.stream()
                .map(id -> statusMap.getOrDefault(id, false))
                .toList();
    }

}
