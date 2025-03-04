package com.clokey.server.domain.notification.application;

import com.clokey.server.domain.term.application.MemberTermRepositoryService;
import com.clokey.server.domain.term.domain.repository.MemberTermRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.history.application.CommentRepositoryService;
import com.clokey.server.domain.history.application.HistoryRepositoryService;
import com.clokey.server.domain.history.domain.entity.Comment;
import com.clokey.server.domain.history.exception.validator.HistoryLikedValidator;
import com.clokey.server.domain.member.application.FollowRepositoryService;
import com.clokey.server.domain.member.application.MemberRepositoryService;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.model.entity.enums.MemberStatus;
import com.clokey.server.domain.model.entity.enums.ReadStatus;
import com.clokey.server.domain.model.entity.enums.RedirectType;
import com.clokey.server.domain.model.entity.enums.Season;
import com.clokey.server.domain.notification.converter.NotificationConverter;
import com.clokey.server.domain.notification.domain.entity.ClokeyNotification;
import com.clokey.server.domain.notification.dto.NotificationResponseDTO;
import com.clokey.server.domain.notification.exception.NotificationException;
import com.clokey.server.global.error.code.status.ErrorStatus;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final HistoryLikedValidator historyLikedValidator;
    private final HistoryRepositoryService historyRepositoryService;
    private final MemberRepositoryService memberRepositoryService;
    private final FirebaseMessaging firebaseMessaging;
    private final NotificationRepositoryService notificationRepositoryService;
    private final FollowRepositoryService followRepositoryService;
    private final CommentRepositoryService commentRepositoryService;
    private final MemberTermRepositoryService memberTermRepositoryService;

    private static final Long NOTIFICATION_MEMBER_TERM_NUM = 5L;

    private static final String HISTORY_LIKED_NOTIFICATION_CONTENT = "%s님이 회원님의 기록을 좋아합니다.";
    private static final String NEW_FOLLOWER_NOTIFICATION_CONTENT = "%s님이 회원님의 옷장을 팔로우하기 시작했습니다.";
    private static final String HISTORY_COMMENT_NOTIFICATION_CONTENT = "%s님이 회원님의 기록에 댓글을 남겼습니다 : %s";
    private static final String COMMENT_REPLY_CONTENT = "%s님이 회원님의 댓글에 답장을 남겼습니다 : %s";

    private static final String ONE_YEAR_AGO_NOTIFICATION = "1년전 오늘의 기록이 도착했습니다!\n과거의 스타일링을 되돌아보세요";
    private static final String ONE_YEAR_AGO_NOTIFICATION_IMAGE_URL = "https://clokeybucket.s3.ap-northeast-2.amazonaws.com/clock.png";

    private static final String SPRING_SEASON_NOTIFICATION = "봄이 다가오고 있어요!\n얇은 옷들을 꺼낼 시간입니다!";
    private static final String SUMMER_SEASON_NOTIFICATION = "여름이 다가오고 있어요!\n시원한 옷들을 꺼낼 시간입니다!";
    private static final String FALL_SEASON_NOTIFICATION = "가을이 다가오고 있어요!\n따뜻한 옷들을 꺼낼 시간입니다!";
    private static final String WINTER_SEASON_NOTIFICATION = "겨울이 다가오고 있어요!\n두꺼운 옷들을 꺼낼 시간입니다!";
    private static final String SEASON_NOTIFICATION_IMAGE_URL = "https://clokeybucket.s3.ap-northeast-2.amazonaws.com/cloth.png";

    private static final String COLDER_THAN_YESTERDAY_NOTIFICATION = "기온이 어제보다 %d도 낮아요!\n오늘은 더 두꺼운 옷을 입어볼까요?";
    private static final String WARMER_THAN_YESTERDAY_NOTIFICATION = "기온이 어제보다 %d도 높아요!\n오늘은 더 얇은 옷을 입어볼까요?";
    private static final String SAME_AS_YESTERDAY_NOTIFICATION = "기온이 어제와 동일해요!\n어제와 비슷하게 입어볼까요?";
    private static final String TODAY_TEMPERATURE_NOTIFICATION_URL = "https://clokeybucket.s3.ap-northeast-2.amazonaws.com/temperature.png";

    @Override
    @Transactional(readOnly = true)
    public NotificationResponseDTO.UnReadNotificationCheckResult checkUnReadNotifications(Long memberId) {
        return NotificationResponseDTO.UnReadNotificationCheckResult.builder()
                .unReadNotificationExist(notificationRepositoryService.existsByMemberIdAndReadStatus(memberId, ReadStatus.NOT_READ))
                .build();
    }

    @Override
    @Transactional
    public void readNotification(Long notificationId, Long memberId) {
        checkMyNotification(notificationId, memberId);
        ClokeyNotification notification = notificationRepositoryService.findById(notificationId);
        notification.readNotification();
    }

    @Override
    @Transactional
    public void readAllNotification(Long memberId) {
        notificationRepositoryService.readAllByMemberId(memberId);
    }

    private void checkMyNotification(Long notificationId, Long memberId) {
        if (!notificationRepositoryService.findById(notificationId).getMember().getId().equals(memberId)) {
            throw new NotificationException(ErrorStatus.NOT_MY_NOTIFICATION);
        }
    }

    @Override
    @Transactional
    public NotificationResponseDTO.HistoryLikeNotificationResult sendHistoryLikeNotification(Long memberId, Long historyId) {

        historyLikedValidator.validateIsLiked(historyId, memberId, true);

        Member historyWriter = historyRepositoryService.findById(historyId).getMember();
        Member likedMember = memberRepositoryService.findMemberById(memberId);

        if (historyWriter.equals(likedMember)) {
            return null;
        }

        if (ableToSendNotification(historyWriter)) {
            String content = String.format(HISTORY_LIKED_NOTIFICATION_CONTENT, likedMember.getNickname());
            String likedMemberProfileUrl = likedMember.getProfileImageUrl();

            Notification notification = Notification.builder()
                    .setBody(content)
                    .setImage(likedMemberProfileUrl)
                    .build();

            Message message = Message.builder()
                    .setToken(historyWriter.getDeviceToken())
                    .setNotification(notification)
                    .putData("historyId", String.valueOf(historyId))
                    .build();
            try {
                firebaseMessaging.send(message);
            } catch (FirebaseMessagingException e) {
                throw new NotificationException(ErrorStatus.NOTIFICATION_FIREBASE_ERROR);
            }
            ClokeyNotification clokeyNotification = ClokeyNotification.builder()
                    .member(historyWriter)
                    .content(content)
                    .notificationImageUrl(likedMemberProfileUrl)
                    .redirectInfo(String.valueOf(historyId))
                    .redirectType(RedirectType.HISTORY_REDIRECT)
                    .readStatus(ReadStatus.NOT_READ)
                    .build();

            notificationRepositoryService.save(clokeyNotification);

            return NotificationResponseDTO.HistoryLikeNotificationResult.builder()
                    .content(content)
                    .historyId(historyId)
                    .memberProfileUrl(likedMemberProfileUrl)
                    .build();
        }
        return null;
    }

    @Override
    @Transactional
    public NotificationResponseDTO.NewFollowerNotificationResult sendNewFollowerNotification(String followedMemberClokeyId, Long followingMemberId) {

        Member followedMember = memberRepositoryService.findMemberByClokeyId(followedMemberClokeyId);
        Member followingMember = memberRepositoryService.findMemberById(followingMemberId);

        checkFollowing(followingMemberId, followedMember.getId());

        if (ableToSendNotification(followedMember)) {
            String content = String.format(NEW_FOLLOWER_NOTIFICATION_CONTENT, followingMember.getNickname());
            String followingMemberProfileUrl = followingMember.getProfileImageUrl();

            Notification notification = Notification.builder()
                    .setBody(content)
                    .setImage(followingMemberProfileUrl)
                    .build();

            Message message = Message.builder()
                    .setToken(followedMember.getDeviceToken())
                    .setNotification(notification)
                    .putData("clokeyID", String.valueOf(followingMember.getClokeyId()))
                    .build();

            try {
                firebaseMessaging.send(message);
            } catch (FirebaseMessagingException e) {
                throw new NotificationException(ErrorStatus.NOTIFICATION_FIREBASE_ERROR);
            }

            ClokeyNotification clokeyNotification = ClokeyNotification.builder()
                    .member(followedMember)
                    .content(content)
                    .notificationImageUrl(followingMemberProfileUrl)
                    .redirectInfo(followingMember.getClokeyId())
                    .redirectType(RedirectType.MEMBER_REDIRECT)
                    .readStatus(ReadStatus.NOT_READ)
                    .build();

            notificationRepositoryService.save(clokeyNotification);

            return NotificationResponseDTO.NewFollowerNotificationResult.builder()
                    .content(content)
                    .memberProfileUrl(followingMemberProfileUrl)
                    .clokeyId(followingMember.getClokeyId())
                    .build();
        }
        return null;
    }


    private void checkFollowing(Long followingId, Long followedId) {
        if (!followRepositoryService.existsByFollowing_IdAndFollowed_Id(followingId, followedId)) {
            throw new NotificationException(ErrorStatus.NOTIFICATION_NOT_FOLLOWING);
        }
    }

    @Override
    @Transactional
    public NotificationResponseDTO.HistoryCommentNotificationResult sendHistoryCommentNotification(Long historyId, Long commentId, Long memberId) {


        checkMyComment(commentId, memberId);
        checkHistoryComment(commentId, historyId);

        Member historyWriter = historyRepositoryService.findById(historyId).getMember();
        Member commentWriter = memberRepositoryService.findMemberById(memberId);
        if (historyWriter.equals(commentWriter)) {
            return null;
        }
        Comment writtenComment = commentRepositoryService.findById(commentId);

        if (ableToSendNotification(historyWriter)) {
            String content = String.format(HISTORY_COMMENT_NOTIFICATION_CONTENT, commentWriter.getNickname(), writtenComment.getContent());
            String commentWriterProfileUrl = commentWriter.getProfileImageUrl();

            Notification notification = Notification.builder()
                    .setBody(content)
                    .setImage(commentWriterProfileUrl)
                    .build();

            Message message = Message.builder()
                    .setToken(historyWriter.getDeviceToken())
                    .setNotification(notification)
                    .putData("historyId", String.valueOf(historyId))
                    .build();
            try {
                firebaseMessaging.send(message);
            } catch (FirebaseMessagingException e) {
                throw new NotificationException(ErrorStatus.NOTIFICATION_FIREBASE_ERROR);
            }

            ClokeyNotification clokeyNotification = ClokeyNotification.builder()
                    .member(historyWriter)
                    .content(content)
                    .notificationImageUrl(commentWriterProfileUrl)
                    .redirectInfo(String.valueOf(historyId))
                    .redirectType(RedirectType.HISTORY_REDIRECT)
                    .readStatus(ReadStatus.NOT_READ)
                    .build();

            notificationRepositoryService.save(clokeyNotification);

            return NotificationResponseDTO.HistoryCommentNotificationResult.builder()
                    .content(content)
                    .historyId(historyId)
                    .memberProfileUrl(commentWriterProfileUrl)
                    .build();

        }


        return null;
    }

    private void checkMyComment(Long commentId, Long memberId) {
        if (!commentRepositoryService.existsByIdAndMemberId(commentId, memberId)) {
            throw new NotificationException(ErrorStatus.NOTIFICATION_NOT_MY_COMMENT);
        }
    }

    private void checkHistoryComment(Long commentId, Long historyId) {
        if (!commentRepositoryService.existsByIdAndHistoryId(commentId, historyId)) {
            throw new NotificationException(ErrorStatus.NOTIFICATION_COMMENT_NOT_FROM_HISTORY);
        }
    }

    @Override
    @Transactional
    public NotificationResponseDTO.ReplyNotificationResult sendReplyNotification(Long commentId, Long replyId, Long memberId) {

        checkMyComment(replyId, memberId);
        checkParentComment(commentId, replyId);

        Comment writtenComment = commentRepositoryService.findById(commentId);
        Member commentWriter = writtenComment.getMember();
        Comment writtenReply = commentRepositoryService.findById(replyId);
        Member replyWriter = writtenReply.getMember();
        if (commentWriter.equals(replyWriter)) {
            return null;
        }

        if (ableToSendNotification(commentWriter)) {
            String content = String.format(COMMENT_REPLY_CONTENT, replyWriter.getNickname(), writtenReply.getContent());
            String replyWriterProfileUrl = replyWriter.getProfileImageUrl();
            Long historyId = commentRepositoryService.findById(commentId).getHistory().getId();

            Notification notification = Notification.builder()
                    .setBody(content)
                    .setImage(replyWriterProfileUrl)
                    .build();

            Message message = Message.builder()
                    .setToken(commentWriter.getDeviceToken())
                    .setNotification(notification)
                    .putData("historyId", String.valueOf(historyId))
                    .build();
            try {
                firebaseMessaging.send(message);
            } catch (FirebaseMessagingException e) {
                throw new NotificationException(ErrorStatus.NOTIFICATION_FIREBASE_ERROR);
            }

            ClokeyNotification clokeyNotification = ClokeyNotification.builder()
                    .member(commentWriter)
                    .content(content)
                    .notificationImageUrl(replyWriterProfileUrl)
                    .redirectInfo(String.valueOf(historyId))
                    .redirectType(RedirectType.HISTORY_REDIRECT)
                    .readStatus(ReadStatus.NOT_READ)
                    .build();

            notificationRepositoryService.save(clokeyNotification);

            return NotificationResponseDTO.ReplyNotificationResult.builder()
                    .content(content)
                    .memberProfileUrl(replyWriterProfileUrl)
                    .historyId(historyId)
                    .isMyHistory(writtenComment.getHistory().getMember().equals(commentWriter))
                    .build();
        }

        return null;
    }

    private void checkParentComment(Long commentId, Long replyId) {
        Comment reply = commentRepositoryService.findById(replyId);
        if (!reply.getComment().getId().equals(commentId)) {
            throw new NotificationException(ErrorStatus.NOTIFICATION_NOT_PARENT_COMMENT_OF_REPLY);
        }
    }


    @Override
    public void sendOneYearAgoNotification(Long memberId) {

        Member member = memberRepositoryService.findMemberById(memberId);

        if (ableToSendNotification(member)) {
            sendNotifications(ONE_YEAR_AGO_NOTIFICATION, ONE_YEAR_AGO_NOTIFICATION_IMAGE_URL, member.getDeviceToken());
        }
    }

    @Override
    public void sendTodayTemperatureNotification(Integer temperatureDiff, Long memberId) {

        Member member = memberRepositoryService.findMemberById(memberId);

        if (!ableToSendNotification(member)) {
            return;
        }

        if (temperatureDiff > 0) {
            sendNotifications(String.format(WARMER_THAN_YESTERDAY_NOTIFICATION, Math.abs(temperatureDiff)), TODAY_TEMPERATURE_NOTIFICATION_URL, member.getDeviceToken());
        } else if (temperatureDiff < 0) {
            sendNotifications(String.format(COLDER_THAN_YESTERDAY_NOTIFICATION, Math.abs(temperatureDiff)), TODAY_TEMPERATURE_NOTIFICATION_URL, member.getDeviceToken());
        } else {
            sendNotifications(SAME_AS_YESTERDAY_NOTIFICATION, TODAY_TEMPERATURE_NOTIFICATION_URL, member.getDeviceToken());
        }

    }

    @Override
    public void sendSeasonsNotification(Season season, Long memberId) {

        Member member = memberRepositoryService.findMemberById(memberId);

        if (!ableToSendNotification(member)) {
            return;
        }

        if (season.equals(Season.SPRING)) {
            sendNotifications(SPRING_SEASON_NOTIFICATION, SEASON_NOTIFICATION_IMAGE_URL, member.getDeviceToken());
        } else if (season.equals(Season.SUMMER)) {
            sendNotifications(SUMMER_SEASON_NOTIFICATION, SEASON_NOTIFICATION_IMAGE_URL, member.getDeviceToken());
        } else if (season.equals(Season.FALL)) {
            sendNotifications(FALL_SEASON_NOTIFICATION, SEASON_NOTIFICATION_IMAGE_URL, member.getDeviceToken());
        } else {
            sendNotifications(WINTER_SEASON_NOTIFICATION, SEASON_NOTIFICATION_IMAGE_URL, member.getDeviceToken());
        }
    }

    @Override
    public NotificationResponseDTO.GetNotificationResult getNotifications(Long memberId, Integer page) {
        // memberId로 알림을 조회해서 반환
        Pageable pageable = PageRequest.of(page - 1, 30);
        List<ClokeyNotification> notificationList = notificationRepositoryService.findNotificationsByMemberId(memberId, pageable);
        return NotificationConverter.toNotificationResult(notificationList, pageable);
    }

    private void sendNotifications(String content, String imageUrl, String deviceToken) {
        Notification notification = Notification.builder()
                .setBody(content)
                .setImage(imageUrl)
                .build();

        Message message = Message.builder()
                .setToken(deviceToken)
                .setNotification(notification)
                .build();
        try {
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            throw new NotificationException(ErrorStatus.NOTIFICATION_FIREBASE_ERROR);
        }
    }

    private boolean ableToSendNotification(Member member) {
        return member.getStatus() != MemberStatus.INACTIVE && memberTermRepositoryService.existsByMemberIdAndTermId(member.getId(),NOTIFICATION_MEMBER_TERM_NUM) && member.getRefreshToken() != null;
    }
}
