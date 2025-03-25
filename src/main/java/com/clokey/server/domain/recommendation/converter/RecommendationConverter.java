package com.clokey.server.domain.recommendation.converter;

import com.clokey.server.domain.cloth.domain.entity.Cloth;
import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.recommendation.dto.RecommendationResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.util.Pair;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class RecommendationConverter {
    public static RecommendationResponseDTO.DailyClothResult toDailyClothResult(Cloth cloth){
        return RecommendationResponseDTO.DailyClothResult.builder()
                .clothId(cloth.getId())
                .imageUrl(cloth.getImage().getImageUrl())
                .clothName(cloth.getName())
                .build();
    }

    public static RecommendationResponseDTO.RecommendCacheResult toRecommendCacheDTO(String imageUrl, Long memberId, String subtitle, String hashtag) {
        return RecommendationResponseDTO.RecommendCacheResult.builder()
                .imageUrl(imageUrl)
                .memberId(memberId)
                .subTitle(subtitle)
                .hashtag(hashtag)
                .date(LocalDateTime.now())
                .build();
    }

    public static List<RecommendationResponseDTO.ClosetResult> toClosetDTO(Map<Pair<Member, LocalDate>, List<Cloth>> groupedClothes) {
        return groupedClothes.entrySet().stream()
                .map(entry -> {
                    Member closetOwner = entry.getKey().getFirst();
                    LocalDate date = entry.getKey().getSecond();
                    List<Cloth> groupedClothList = entry.getValue();

                    List<Long> clothesIds = groupedClothList.stream()
                            .map(Cloth::getId)
                            .collect(Collectors.toList());

                    List<String> images = groupedClothList.stream()
                            .map(cloth -> cloth.getImage() != null ? cloth.getImage().getImageUrl() : null)
                            .collect(Collectors.toList());

                    return new RecommendationResponseDTO.ClosetResult(
                            closetOwner.getClokeyId(),
                            closetOwner.getProfileImageUrl(),
                            clothesIds,
                            images,
                            date
                    );
                })
                .sorted((c1, c2) -> c2.getDate().compareTo(c1.getDate()))
                .collect(Collectors.toList());
    }

    public static List<RecommendationResponseDTO.ClosetCacheResult> toClosetCacheDTO(Map<Pair<Member, LocalDate>, List<Cloth>> groupedClothes) {
        return groupedClothes.entrySet().stream()
                .map(entry -> {
                    Member closetOwner = entry.getKey().getFirst();
                    LocalDate date = entry.getKey().getSecond();
                    List<Cloth> groupedClothList = entry.getValue();

                    List<Pair<Long, String>> clothData = groupedClothList.stream()
                            .limit(3)
                            .map(cloth -> Pair.of(cloth.getId(), cloth.getImage() != null ? cloth.getImage().getImageUrl() : null))
                            .toList();

                    List<Long> clothesIds = clothData.stream()
                            .map(Pair::getFirst)
                            .collect(Collectors.toList());

                    List<String> images = clothData.stream()
                            .map(Pair::getSecond)
                            .collect(Collectors.toList());

                    return new RecommendationResponseDTO.ClosetCacheResult(
                            closetOwner.getId(),
                            clothesIds,
                            images,
                            date
                    );
                })
                .sorted((c1, c2) -> c2.getDate().compareTo(c1.getDate()))
                .collect(Collectors.toList());
    }

    public static List<RecommendationResponseDTO.CalendarResult> toCalendarDTO(Page<History> historyPage, Map<History, List<String>> historyImageMap) {
        return historyPage.getContent().stream()
                .map(history -> {
                    Member historyOwner = history.getMember();
                    List<String> imageUrls = historyImageMap.getOrDefault(history, Collections.emptyList());
                    String imageUrl = imageUrls.isEmpty() ? null : imageUrls.get(0);

                    return new RecommendationResponseDTO.CalendarResult(
                            history.getHistoryDate(),
                            historyOwner.getClokeyId(),
                            historyOwner.getProfileImageUrl(),
                            history.getId(),
                            imageUrl
                    );
                })
                .collect(Collectors.toList());
    }

    public static List<RecommendationResponseDTO.CalendarCacheResult> toCalendarCacheDTO(List<History> historyPage, Map<History, List<String>> historyImageMap) {
        return historyPage.stream()
                .map(history -> {
                    Member historyOwner = history.getMember();
                    List<String> imageUrls = historyImageMap.getOrDefault(history, Collections.emptyList());
                    String imageUrl = imageUrls.isEmpty() ? null : imageUrls.get(0);

                    return new RecommendationResponseDTO.CalendarCacheResult(
                            historyOwner.getId(),
                            history.getHistoryDate(),
                            history.getId(),
                            imageUrl
                    );
                })
                .limit(2)
                .collect(Collectors.toList());
    }

    public static List<RecommendationResponseDTO.PeopleCacheResult> toPeopleCacheDTO(List<History> recommendedHistories, Map<Long, String> historyImageMap) {
        return recommendedHistories.stream()
                .distinct()
                .limit(4)
                .map(recommendedHistory -> new RecommendationResponseDTO.PeopleCacheResult(
                        recommendedHistory.getMember().getId(),
                        historyImageMap.get(recommendedHistory.getId()),
                        recommendedHistory.getId()
                ))
                .collect(Collectors.toList());
    }

    public static <T> RecommendationResponseDTO.DailyNewsAllResult<T> toDailyNewsAllResult(Page<T> page) {
        return RecommendationResponseDTO.DailyNewsAllResult.<T>builder()
                .dailyNewsResult(page.getContent())
                .totalPage(page.getTotalPages())
                .totalElements((int) page.getTotalElements())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }

    public static RecommendationResponseDTO.DailyNewsResult toDailyNewsResult(List<RecommendationResponseDTO.RecommendResult> recommendList, List<RecommendationResponseDTO.ClosetResult> closetList, List<RecommendationResponseDTO.CalendarResult> calendarList, List<RecommendationResponseDTO.PeopleResult> peopleList, Integer followingCount) {
        return RecommendationResponseDTO.DailyNewsResult.builder()
                .recommend(recommendList)
                .closet(closetList)
                .calendar(calendarList)
                .people(peopleList)
                .followingCount(followingCount)
                .build();
    }

    public static RecommendationResponseDTO.LastYearHistoryResult toLastYearHistoryResult(Long historyId, LocalDate date, List<String> historyImageUrls, Member member, Boolean isMine) {
        return RecommendationResponseDTO.LastYearHistoryResult.builder()
                .historyId(historyId)
                .date(date)
                .nickName(member.getNickname())
                .imageUrls(historyImageUrls)
                .isMine(isMine)
                .build();
    }
    public static List<RecommendationResponseDTO.RecommendResult> toRecommendResult(
            List<RecommendationResponseDTO.RecommendCacheResult> cachedRecommend, Map<Long, Member> memberMap, Set<Long> blockingMembers) {
        return cachedRecommend.stream()
                .filter(recommendCache -> !blockingMembers.contains(recommendCache.getMemberId()))
                .map(recommendCache -> {
                    Member member = Optional.ofNullable(memberMap.get(recommendCache.getMemberId())).orElse(new Member());
                    return RecommendationResponseDTO.RecommendResult.builder()
                            .imageUrl(recommendCache.getImageUrl())
                            .subTitle(member.getNickname() + recommendCache.getSubTitle())
                            .hashtag(recommendCache.getHashtag())
                            .date(recommendCache.getDate())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public static List<RecommendationResponseDTO.ClosetResult> toClosetResult(
            List<RecommendationResponseDTO.ClosetCacheResult> cachedClosets, Map<Long, Member> memberMap) {
        return cachedClosets.stream()
                .map(closetCache -> {
                    Member member = Optional.ofNullable(memberMap.get(closetCache.getMemberId())).orElse(new Member());
                    return RecommendationResponseDTO.ClosetResult.builder()
                            .clokeyId(member.getClokeyId())
                            .profileImage(member.getProfileImageUrl())
                            .clothesId(closetCache.getClothesId())
                            .images(closetCache.getImages())
                            .date(closetCache.getDate())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public static List<RecommendationResponseDTO.CalendarResult> toCalendarResult(
            List<RecommendationResponseDTO.CalendarCacheResult> cachedCalendars, Map<Long, Member> memberMap) {
        return cachedCalendars.stream()
                .map(calendarCache -> {
                    Member member = Optional.ofNullable(memberMap.get(calendarCache.getMemberId())).orElse(new Member());
                    return RecommendationResponseDTO.CalendarResult.builder()
                            .clokeyId(member.getClokeyId())
                            .profileImage(member.getProfileImageUrl())
                            .date(calendarCache.getDate())
                            .historyId(calendarCache.getHistoryId())
                            .imageUrl(calendarCache.getImageUrl())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public static List<RecommendationResponseDTO.PeopleResult> toPeopleResult(
            List<RecommendationResponseDTO.PeopleCacheResult> cachedPeople, Map<Long, Member> memberMap, Set<Long> blockingMembers) {
        return cachedPeople.stream()
                .filter(peopleCache -> !blockingMembers.contains(peopleCache.getMemberId()))
                .map(peopleCache -> {
                    Member member = Optional.ofNullable(memberMap.get(peopleCache.getMemberId())).orElse(new Member());
                    return RecommendationResponseDTO.PeopleResult.builder()
                            .clokeyId(member.getClokeyId())
                            .profileImage(member.getProfileImageUrl())
                            .imageUrl(peopleCache.getImageUrl())
                            .historyId(peopleCache.getHistoryId())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
