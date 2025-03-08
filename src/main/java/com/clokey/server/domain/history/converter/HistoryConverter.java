package com.clokey.server.domain.history.converter;

import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

    public static HistoryResponseDTO.DailyHistoryResult toDayViewResult(History history, List<String> imageUrl, List<String> hashtags, int likeCount, boolean isLiked, List<Cloth> cloths, Long commentCount) {
        return HistoryResponseDTO.DailyHistoryResult.builder()
                .memberId(history.getMember().getId())
                .historyId(history.getId())
                .contents(history.getContent())
                .memberImageUrl(history.getMember().getProfileImageUrl())
                .imageUrl(imageUrl)
                .hashtags(hashtags)
                .visibility(history.getVisibility().equals(Visibility.PUBLIC))
                .likeCount(likeCount)
                .commentCount(commentCount)
                .isLiked(isLiked)
                .date(history.getHistoryDate())
                .nickName(history.getMember().getNickname())
                .clokeyId(history.getMember().getClokeyId())
                .cloths(cloths.stream()
                        .map(HistoryConverter::toHistoryCloth)
                        .toList())
                .build();
    }

    private static HistoryResponseDTO.HistoryClothResult toHistoryCloth(Cloth cloth) {
        return HistoryResponseDTO.HistoryClothResult.builder()
                .clothId(cloth.getId())
                .clothImageUrl(cloth.getImage().getImageUrl())
                .clothName(cloth.getName())
                .build();
    }

    public static HistoryResponseDTO.MonthViewResult toMonthViewResult(Long memberId, String nickName, List<History> histories, List<String> historyFirstImageUrls) {

        List<HistoryResponseDTO.HistoryResult> HistoryResults = new ArrayList<>();

        for (int i = 0; i < histories.size(); i++) {
            History history = histories.get(i);
            String historyImageUrl = historyFirstImageUrls.get(i);

            HistoryResults.add(toHistoryResult(history, historyImageUrl));
        }

        return HistoryResponseDTO.MonthViewResult.builder()
                .memberId(memberId)
                .nickName(nickName)
                .histories(HistoryResults)
                .build();
    }

    private static HistoryResponseDTO.HistoryResult toHistoryResult(History history, String historyImageUrl) {
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

    public static HistoryResponseDTO.HistoryCommentResult toHistoryCommentResult(Page<Comment> comments, List<List<Comment>> replies) {
        return HistoryResponseDTO.HistoryCommentResult.builder()
                .comments(toCommentResultList(comments, replies))
                .totalPage(comments.getTotalPages())
                .totalElements(comments.getNumberOfElements()
                        + replies.stream()
                        .mapToInt(List::size)
                        .sum())
                .isFirst(comments.isFirst())
                .isLast(comments.isLast())
                .build();
    }

    ;


    private static List<HistoryResponseDTO.CommentResult> toCommentResultList(Page<Comment> comments, List<List<Comment>> replies) {
        return IntStream.range(0, comments.getContent().size())
                .mapToObj(i -> {
                    Comment comment = comments.getContent().get(i);
                    List<Comment> replyList = replies.get(i);
                    return HistoryResponseDTO.CommentResult.builder()
                            .commentId(comment.getId())
                            .clokeyId(comment.getMember().getClokeyId())
                            .nickName(comment.getMember().getNickname())
                            .userImageUrl(comment.getMember().getProfileImageUrl())
                            .content(comment.getContent())
                            .replyResults(toReplyResultList(replyList))
                            .build();
                })
                .toList();
    }

    private static List<HistoryResponseDTO.ReplyResult> toReplyResultList(List<Comment> replies) {
        return replies.stream()
                .map(reply -> HistoryResponseDTO.ReplyResult.builder()
                        .commentId(reply.getId())
                        .clokeyId(reply.getMember().getClokeyId())
                        .nickName(reply.getMember().getNickname())
                        .userImageUrl(reply.getMember().getProfileImageUrl())
                        .content(reply.getContent())
                        .build())
                .toList();
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
}
