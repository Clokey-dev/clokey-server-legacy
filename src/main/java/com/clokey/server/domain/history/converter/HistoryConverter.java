package com.clokey.server.domain.history.converter;

import com.clokey.server.domain.history.dto.projection.DailyHistoryClothProjectionDTO;
import com.clokey.server.domain.history.dto.projection.HistoryCommentProjectionDTO;
import com.clokey.server.domain.history.dto.projection.HistoryProjectionDTO;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.clokey.server.domain.cloth.domain.entity.Cloth;
import com.clokey.server.domain.history.domain.document.HistoryDocument;
import com.clokey.server.domain.history.domain.entity.Comment;
import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.history.dto.HistoryRequestDTO;
import com.clokey.server.domain.history.dto.HistoryResponseDTO;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.model.entity.enums.Visibility;

public class HistoryConverter {

    public static <T> List<HistoryResponseDTO.HistoryPreview> toHistoryPreviewList(Page<? extends T> page) {
        return Optional.ofNullable(page.getContent()).orElse(Collections.emptyList())
                .stream()
                .map(item -> {
                            HistoryDocument doc = (HistoryDocument) item;
                            return HistoryResponseDTO.HistoryPreview.builder()
                                    .id(doc.getId())
                                    .imageUrl(doc.getImageUrl())
                                    .build();
                        }
                )
                .collect(Collectors.toList());
    }

    public static HistoryResponseDTO.HistoryPreviewListResult toHistoryPreviewListResult(Page<?> page,
                                                                                         List<HistoryResponseDTO.HistoryPreview> historyPreviews) {
        return HistoryResponseDTO.HistoryPreviewListResult.builder()
                .historyPreviews(historyPreviews)
                .totalPage(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }

    public static HistoryResponseDTO.HistoryLikedListResult toHistoryLikedListResult(Page<History> histories, Map<Long, String> folderImageMap, Long currentMemberId) {
        List<HistoryResponseDTO.HistoryLikedPreview> historyPreviews = histories.getContent().stream()
                .map(history -> new HistoryResponseDTO.HistoryLikedPreview(
                        history.getId(),
                        folderImageMap.get(history.getId()),
                        history.getMember().getId().equals(currentMemberId)
                ))
                .collect(Collectors.toList());

        return HistoryResponseDTO.HistoryLikedListResult.builder()
                .historyPreviews(historyPreviews)
                .totalPage(histories.getTotalPages())
                .totalElements((int) histories.getTotalElements())
                .isFirst(histories.isFirst())
                .isLast(histories.isLast())
                .build();
    }

    public static HistoryResponseDTO.DailyHistoryResult toDayViewResult(History history, Member member, List<String> imageUrl, List<String> hashtags, boolean isLiked, List<DailyHistoryClothProjectionDTO> cloths, Long commentCount) {
        return HistoryResponseDTO.DailyHistoryResult.builder()
                .memberId(member.getId())
                .historyId(history.getId())
                .contents(history.getContent())
                .memberImageUrl(member.getProfileImageUrl())
                .imageUrl(imageUrl)
                .hashtags(hashtags)
                .visibility(history.getVisibility().equals(Visibility.PUBLIC))
                .likeCount(history.getLikes())
                .commentCount(commentCount)
                .isLiked(isLiked)
                .date(history.getHistoryDate())
                .nickName(member.getNickname())
                .clokeyId(member.getClokeyId())
                .cloths(cloths.stream()
                        .map(HistoryConverter::toHistoryCloth)
                        .toList())
                .build();
    }

    private static HistoryResponseDTO.HistoryClothResult toHistoryCloth(DailyHistoryClothProjectionDTO dailyHistoryClothProjectionDTO) {
        return HistoryResponseDTO.HistoryClothResult.builder()
                .clothId(dailyHistoryClothProjectionDTO.getClothId())
                .clothImageUrl(dailyHistoryClothProjectionDTO.getClothImageUrl())
                .clothName(dailyHistoryClothProjectionDTO.getClothName())
                .build();
    }

    public static HistoryResponseDTO.MonthViewResult toMonthViewResult(Long memberId, String nickName, List<HistoryProjectionDTO> histories, List<String> historyFirstImageUrls) {

        List<HistoryResponseDTO.HistoryResult> HistoryResults = new ArrayList<>();

        for (int i = 0; i < histories.size(); i++) {
            HistoryProjectionDTO history = histories.get(i);
            String historyImageUrl = historyFirstImageUrls.get(i);

            HistoryResults.add(toHistoryResult(history, historyImageUrl));
        }

        return HistoryResponseDTO.MonthViewResult.builder()
                .memberId(memberId)
                .nickName(nickName)
                .histories(HistoryResults)
                .build();
    }

    private static HistoryResponseDTO.HistoryResult toHistoryResult(HistoryProjectionDTO history, String historyImageUrl) {
        return HistoryResponseDTO.HistoryResult.builder()
                .historyId(history.getId())
                .date(history.getHistoryDate())
                .imageUrl(historyImageUrl)
                .build();
    }


    public static HistoryResponseDTO.LikeResult toLikeResult(History history, boolean isLiked) {
        return HistoryResponseDTO.LikeResult.builder()
                .historyId(history.getId())
                .isLiked(!isLiked)
                .likeCount(history.getLikes())
                .build();
    }

    public static HistoryResponseDTO.HistoryCommentResult toHistoryCommentResult(
            List<HistoryCommentProjectionDTO> flatComments,
            int page,
            int pageSize,
            int totalRootCount
    ) {
        // 부모 댓글 id를 기준으로 그룹
        Map<Long, List<HistoryCommentProjectionDTO>> repliesGrouped = flatComments.stream()
                .filter(dto -> !dto.isRoot()) // 대댓글
                .collect(Collectors.groupingBy(HistoryCommentProjectionDTO::getParentId));

        // 댓글과 대댓글 연결해주기
        List<HistoryResponseDTO.CommentResult> rootResults = flatComments.stream()
                .filter(HistoryCommentProjectionDTO::isRoot)
                .map(root -> HistoryResponseDTO.CommentResult.builder()
                        .commentId(root.getCommentId())
                        .content(root.getContent())
                        .clokeyId(root.getClokeyId())
                        .nickName(root.getNickname())
                        .userImageUrl(root.getProfileImageUrl())
                        .replyResults(
                                repliesGrouped.getOrDefault(root.getCommentId(), List.of()).stream()
                                        .map(reply -> HistoryResponseDTO.ReplyResult.builder()
                                                .commentId(reply.getCommentId())
                                                .content(reply.getContent())
                                                .clokeyId(reply.getClokeyId())
                                                .nickName(reply.getNickname())
                                                .userImageUrl(reply.getProfileImageUrl())
                                                .build())
                                        .toList()
                        )
                        .build())
                .toList();

        int totalPage = (int) Math.ceil((double) totalRootCount / pageSize);
        int totalElements = rootResults.stream()
                .mapToInt(r -> 1 + (r.getReplyResults() != null ? r.getReplyResults().size() : 0))
                .sum();

        return HistoryResponseDTO.HistoryCommentResult.builder()
                .comments(rootResults)
                .totalPage(totalPage)
                .totalElements(totalElements)
                .isFirst(page == 0)
                .isLast(page + 1 == totalPage)
                .build();
    }

    public static HistoryResponseDTO.CommentWriteResult toCommentWriteResult(Comment comment) {
        return HistoryResponseDTO.CommentWriteResult.builder()
                .commentId(comment.getId())
                .build();
    }

    public static History toHistory(HistoryRequestDTO.HistoryCreate request, Member member) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // 패턴 지정

        return History.builder()
                .historyDate(LocalDate.parse(request.getDate(), formatter))
                .likes(0)
                .visibility(request.getVisibility())
                .content(request.getContent())
                .member(member)
                .build();
    }

    public static HistoryResponseDTO.HistoryCreateResult toHistoryCreateResult(History history) {
        return HistoryResponseDTO.HistoryCreateResult.builder()
                .historyId(history.getId())
                .build();
    }

    public static HistoryResponseDTO.LikedUserResults toLikedUserResult(List<Member> members, List<Boolean> followStatus, List<Boolean> isMySelf) {
        List<HistoryResponseDTO.LikedUserResult> likedUserResults = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);
            likedUserResults.add(HistoryResponseDTO.LikedUserResult.builder()
                    .clokeyId(member.getClokeyId())
                    .imageUrl(member.getProfileImageUrl())
                    .followStatus(followStatus.get(i))
                    .memberId(member.getId())
                    .nickname(member.getNickname())
                    .isMe(isMySelf.get(i))
                    .build());
        }
        return HistoryResponseDTO.LikedUserResults.builder()
                .likedUsers(likedUserResults)
                .build();
    }

    public static HistoryResponseDTO.MyCommentResult toMyCommentResult(Comment comment) {
        return HistoryResponseDTO.MyCommentResult.builder()
            .content(comment.getContent())
            .build();
    }

    public static HistoryResponseDTO.HistoryMyCommentResult toHistoryMyCommentResult(History history, List<HistoryResponseDTO.MyCommentResult> commentsList,  Map<Long, String> imageMap) {
        return HistoryResponseDTO.HistoryMyCommentResult.builder()
                .historyId(history.getId())
                .nickname(history.getMember().getNickname())
                .date(history.getHistoryDate())
                .imageUrl(imageMap.get(history.getId()))
                .comments(commentsList)
                .build();
    }


    public static HistoryResponseDTO.HistoryMyCommentListResult toHistoryMyCommentListResult(Page<Comment> commentsPage, List<HistoryResponseDTO.HistoryMyCommentResult> historyMyCommentResults) {
        return HistoryResponseDTO.HistoryMyCommentListResult.builder()
                .histories(historyMyCommentResults)
                .totalPage(commentsPage.getTotalPages())
                .totalElements(commentsPage.getTotalElements())
                .isFirst(commentsPage.isFirst())
                .isLast(commentsPage.isLast())
                .build();
    }
}
