package com.clokey.server.domain.recommendation.application;

import com.clokey.server.domain.history.application.*;
import com.clokey.server.domain.history.domain.repository.HashtagRepository;
import com.clokey.server.domain.history.domain.repository.HistoryRepository;
import com.clokey.server.domain.history.exception.HistoryException;
import com.clokey.server.domain.member.application.BlockRepositoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;

import com.clokey.server.domain.cloth.application.ClothRepositoryService;
import com.clokey.server.domain.cloth.domain.entity.Cloth;
import com.clokey.server.domain.history.domain.entity.HashtagHistory;
import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.history.domain.entity.HistoryImage;
import com.clokey.server.domain.member.application.FollowRepositoryService;
import com.clokey.server.domain.member.application.MemberRepositoryService;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.model.entity.enums.Visibility;
import com.clokey.server.domain.recommendation.converter.RecommendationConverter;
import com.clokey.server.domain.recommendation.dto.RecommendationResponseDTO;
import com.clokey.server.domain.recommendation.exception.RecommendException;
import com.clokey.server.global.error.code.status.ErrorStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final MemberRepositoryService memberRepositoryService;
    private final ClothRepositoryService clothRepositoryService;
    private final HistoryImageRepositoryService historyImageRepositoryService;
    private final FollowRepositoryService followRepositoryService;
    private final HashtagHistoryRepositoryService hashtagHistoryRepositoryService;
    private final BlockRepositoryService blockRepositoryService;
    private final HistoryClothRepositoryService historyClothRepositoryService;
    private final HistoryRepository historyRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper;
    private static final String REDIS_PREFIX_RECOMMEND_CLOTH = "rc:";
    private static final String REDIS_PREFIX_RECOMMEND = "re:";
    private static final String REDIS_PREFIX_CLOSET = "cl:";
    private static final String REDIS_PREFIX_CALENDAR = "ca:";
    private static final String REDIS_PREFIX_PEOPLE = "pe:";
    private final HashtagRepository hashtagRepository;

    @Override
    @Transactional(readOnly = true)
    public RecommendationResponseDTO.DailyClothesResult getRecommendClothes(Long memberId, Integer nowTemp, Integer minTemp, Integer maxTemp) {
        String cacheKeyRecommend = REDIS_PREFIX_RECOMMEND_CLOTH + memberId;

        List<Long> cachedClothIds = getFromRedis(cacheKeyRecommend, Long.class);

        if (cachedClothIds != null && !cachedClothIds.isEmpty()) {
            List<Cloth> cachedClothes = clothRepositoryService.findAllById(cachedClothIds);
            if(cachedClothes.size()==3) {
                List<RecommendationResponseDTO.DailyClothResult> cachedResults = cachedClothes.stream()
                        .map(RecommendationConverter::toDailyClothResult)
                        .collect(Collectors.toList());

                return new RecommendationResponseDTO.DailyClothesResult(cachedResults);
            }
            redisTemplate.delete(cacheKeyRecommend);
        }

        List<Cloth> suitableClothes = clothRepositoryService.findBySuitableClothFilters(memberId, nowTemp, minTemp, maxTemp);
        Set<Cloth> selectedClothes = new HashSet<>();

        Cloth top = findClothByCategory(suitableClothes, 1L, selectedClothes);
        Cloth bottom = findClothByCategory(suitableClothes, 2L, selectedClothes);
        Cloth outer = findClothByCategory(suitableClothes, 3L, selectedClothes);

        if (top == null) {
            top = findClothByCategory(suitableClothes, 4L, selectedClothes);
        }
        if (bottom == null) {
            bottom = findClothByCategory(suitableClothes, 4L, selectedClothes);
        }
        if (outer == null) {
            outer = findClothByCategory(suitableClothes, 4L, selectedClothes);
        }

        if (top == null && bottom == null && outer == null) {
            return new RecommendationResponseDTO.DailyClothesResult(List.of());
        }

        List<Long> clothIds = Stream.of(top, bottom, outer)
                .filter(Objects::nonNull)
                .map(Cloth::getId)
                .collect(Collectors.toList());

        saveToRedis(cacheKeyRecommend, clothIds, Duration.ofHours(3));

        List<RecommendationResponseDTO.DailyClothResult> recommendedClothes = Stream.of(top, bottom, outer)
                .filter(Objects::nonNull)
                .map(RecommendationConverter::toDailyClothResult)
                .collect(Collectors.toList());

        return new RecommendationResponseDTO.DailyClothesResult(recommendedClothes);
    }

    private Cloth findClothByCategory(List<Cloth> clothes, Long parentCategoryId, Set<Cloth> selectedClothes) {
        return clothes.stream()
                .filter(c -> c.getCategory().getParent() != null
                        && c.getCategory().getParent().getId().equals(parentCategoryId)
                        && !selectedClothes.contains(c))
                .findFirst()
                .map(c -> {
                    selectedClothes.add(c);
                    return c;
                })
                .orElse(null);
    }

    @Override
    public RecommendationResponseDTO.DailyNewsAllResult<?> getNewsAll(Long memberId, String section, Integer page) {
        return mapToResponse(memberId, section, page);
    }

    @Override
    public RecommendationResponseDTO.DailyNewsResult getNews(Long memberId) {
        String cacheKeyRecommend = REDIS_PREFIX_RECOMMEND + memberId;
        String cacheKeyCloset = REDIS_PREFIX_CLOSET + memberId;
        String cacheKeyCalendar = REDIS_PREFIX_CALENDAR + memberId;
        String cacheKeyPeople = REDIS_PREFIX_PEOPLE + memberId;

        List<RecommendationResponseDTO.RecommendCacheResult> cachedRecommends = getFromRedis(cacheKeyRecommend, RecommendationResponseDTO.RecommendCacheResult.class);
        List<RecommendationResponseDTO.ClosetCacheResult> cachedClosets = getFromRedis(cacheKeyCloset, RecommendationResponseDTO.ClosetCacheResult.class);
        List<RecommendationResponseDTO.CalendarCacheResult> cachedCalendars = getFromRedis(cacheKeyCalendar, RecommendationResponseDTO.CalendarCacheResult.class);
        List<RecommendationResponseDTO.PeopleCacheResult> cachedPeople = getFromRedis(cacheKeyPeople, RecommendationResponseDTO.PeopleCacheResult.class);

        Member member = memberRepositoryService.findMemberById(memberId);
        List<Member> followingMembers = getFollowingMembers(member.getId());
        Set<Long> blockingMembers = getBlockingMembers(member.getId());

        if (cachedRecommends == null) {
            cachedRecommends = getRecommendList(member, blockingMembers);
            saveToRedis(cacheKeyRecommend, cachedRecommends, Duration.ofMinutes(1));
        }
        if (cachedClosets == null) {
            cachedClosets = getClosetList(followingMembers);
            saveToRedis(cacheKeyCloset, cachedClosets, Duration.ofMinutes(1));
        }
        if (cachedCalendars == null) {
            cachedCalendars = getCalendarList(followingMembers);
            saveToRedis(cacheKeyCalendar, cachedCalendars, Duration.ofMinutes(1));
        }
        if (cachedPeople == null) {
            cachedPeople = getPeopleList(member, blockingMembers);
            saveToRedis(cacheKeyPeople, cachedPeople, Duration.ofMinutes(1));
        }

        Set<Long> memberIds = new HashSet<>();
        cachedRecommends.forEach(r -> memberIds.add(r.getMemberId()));
        cachedClosets.forEach(c -> memberIds.add(c.getMemberId()));
        cachedCalendars.forEach(c -> memberIds.add(c.getMemberId()));
        cachedPeople.forEach(p -> memberIds.add(p.getMemberId()));

        Map<Long, Member> memberMap = memberRepositoryService.findMembersByIds(memberIds);

        List<RecommendationResponseDTO.RecommendResult> recommendResponseList = RecommendationConverter.toRecommendResult(cachedRecommends, memberMap, blockingMembers);
        List<RecommendationResponseDTO.ClosetResult> closetResponseList = RecommendationConverter.toClosetResult(cachedClosets, memberMap);
        List<RecommendationResponseDTO.CalendarResult> calendarResponseList = RecommendationConverter.toCalendarResult(cachedCalendars, memberMap);
        List<RecommendationResponseDTO.PeopleResult> peopleResponseList = RecommendationConverter.toPeopleResult(cachedPeople, memberMap, blockingMembers);

        return RecommendationConverter.toDailyNewsResult(recommendResponseList, closetResponseList, calendarResponseList, peopleResponseList, followingMembers.size());
    }

    private Set<Long> getBlockingMembers(Long id) {
        List<Member> blockingMembers = blockRepositoryService.findAllByBlocker(id, Pageable.unpaged());
        return blockingMembers.stream()
                .map(Member::getId)
                .collect(Collectors.toSet());
    }

    private <T> void saveToRedis(String key, List<T> value, Duration duration) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), duration);
        } catch (Exception e) {
            log.error("Failed to save data to Redis: {}", e.getMessage());
        }
    }

    private <T> List<T> getFromRedis(String key, Class<T> clazz) {
        Object cachedData = redisTemplate.opsForValue().get(key);
        try {
            if (cachedData instanceof String json) {
                return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
            }
        } catch (Exception e) {
            log.error("Failed to get data from Redis: {}", e.getMessage());
        }
        return null;
    }

    private RecommendationResponseDTO.DailyNewsAllResult<?> mapToResponse(Long memberId, String section, Integer page) {
        Member member = memberRepositoryService.findMemberById(memberId);
        List<Member> followingMembers = getFollowingMembers(member.getId());
        if ("closet".equals(section)) {
            Page<RecommendationResponseDTO.ClosetResult> closetPage = getClosetPage(page, followingMembers);
            return RecommendationConverter.toDailyNewsAllResult(closetPage);
        } else if ("calendar".equals(section)) {
            Page<RecommendationResponseDTO.CalendarResult> calendarPage = getCalendarPage(page, followingMembers);
            return RecommendationConverter.toDailyNewsAllResult(calendarPage);
        } else throw new RecommendException(ErrorStatus.NO_SUCH_SECTION);
    }

    private List<RecommendationResponseDTO.RecommendCacheResult> getRecommendList(Member member,  Set<Long> blockingMembers) {
        List<RecommendationResponseDTO.RecommendCacheResult> recommendList = new ArrayList<>();
        String unusedHashtag = hashtagRepository.findRandomUnusedHashtag(member.getId()).orElse(null);

        recommendList.add(RecommendationConverter.toRecommendCacheDTO(
                getHistoryImageUrlByHashtagName(unusedHashtag, blockingMembers),
                member.getId(),
                "님이 시도하지 않은 스타일",
                unusedHashtag
        ));

        String recentHashtag = hashtagHistoryRepositoryService.findLatestTaggedHashtag(member.getId());
        recommendList.add(RecommendationConverter.toRecommendCacheDTO(
                getHistoryImageUrlByHashtagName(recentHashtag, blockingMembers),
                member.getId(),
                "님이 최근 태그한 해시태그",
                recentHashtag
        ));

        String frequentCategory = historyClothRepositoryService.findMostWornCategory(member.getId());
        recommendList.add(RecommendationConverter.toRecommendCacheDTO(
                getHistoryImageUrlByCategoryName(frequentCategory, blockingMembers),
                member.getId(),
                "님이 자주 착용한 카테고리",
                "#"+frequentCategory
        ));

        return recommendList;
    }
    
    private List<RecommendationResponseDTO.ClosetCacheResult> getClosetList(List<Member> followingMembers) {
        List<Cloth> clothesList = clothRepositoryService.findTop6ByMemberInAndVisibilityOrderByCreatedAtDesc(followingMembers, Visibility.PUBLIC);

        Map<Pair<Member, LocalDate>, List<Cloth>> groupedClothes = clothesList.stream()
                .collect(Collectors.groupingBy(
                        cloth -> Pair.of(cloth.getMember(), cloth.getCreatedAt().toLocalDate())
                ));

        return groupedClothes.entrySet().stream()
                .max(Comparator.comparing(entry -> entry.getKey().getSecond()))
                .map(entry -> RecommendationConverter.toClosetCacheDTO(Map.of(entry.getKey(), entry.getValue())))
                .orElse(List.of());
    }

    private Page<RecommendationResponseDTO.ClosetResult> getClosetPage(int page, List<Member> followingMembers) {
        Page<Cloth> clothesPage = clothRepositoryService.findByMemberInAndVisibilityOrderByCreatedAtDesc(
                followingMembers, Visibility.PUBLIC, PageRequest.of(page - 1, 6));

        List<RecommendationResponseDTO.ClosetResult> closetList = RecommendationConverter.toClosetDTO(
                clothesPage.getContent().stream()
                        .collect(Collectors.groupingBy(cloth -> Pair.of(cloth.getMember(), cloth.getCreatedAt().toLocalDate())))
        );

        return new PageImpl<>(closetList, PageRequest.of(page - 1, 6), closetList.size());
    }

    private List<RecommendationResponseDTO.CalendarCacheResult> getCalendarList(List<Member> followingMembers) {
        List<History> historyList = historyRepository.findTop6ByMemberInAndVisibilityAndHistoryDateAfterOrderByHistoryDateDesc(
                followingMembers, Visibility.PUBLIC,LocalDate.now().minusWeeks(2));
        List<Long> historyIds = historyList.stream().map(History::getId).toList();
        Map<History, List<String>> historyImageMap = historyImageRepositoryService.findByHistoryIdIn(historyIds)
                .stream()
                .collect(Collectors.groupingBy(HistoryImage::getHistory, Collectors.mapping(HistoryImage::getImageUrl, Collectors.toList())));

        return RecommendationConverter.toCalendarCacheDTO(historyList, historyImageMap);
    }

    private Page<RecommendationResponseDTO.CalendarResult> getCalendarPage(int page, List<Member> followingMembers) {
        Pageable pageable = PageRequest.of(page - 1, 6);
        Page<History> historyPage = historyRepository.findByMemberInAndVisibilityOrderByHistoryDateDesc(
                followingMembers, Visibility.PUBLIC, pageable);

        List<Long> historyIds = historyPage.getContent().stream().map(History::getId).toList();
        Map<History, List<String>> historyImageMap = historyImageRepositoryService.findByHistoryIdIn(historyIds)
                .stream()
                .collect(Collectors.groupingBy(HistoryImage::getHistory, Collectors.mapping(HistoryImage::getImageUrl, Collectors.toList())));

        List<RecommendationResponseDTO.CalendarResult> calendarList = RecommendationConverter.toCalendarDTO(historyPage, historyImageMap);

        return new PageImpl<>(calendarList, PageRequest.of(page - 1, 6), calendarList.size());
    }

    private List<RecommendationResponseDTO.PeopleCacheResult> getPeopleList(Member member, Set<Long> blockingMembers) {
        List<Long> topFollowingMemberIds = followRepositoryService.findTopFollowingMembers(blockingMembers, member);

        if (topFollowingMemberIds.isEmpty()) {
            return List.of();
        }

        List<History> recommendedMemberHistories = historyRepository.findHistoryByMemberIdIn(topFollowingMemberIds);

        if (recommendedMemberHistories.isEmpty()) {
            return List.of();
        }
        List<Long> historyIds = recommendedMemberHistories.stream()
                .map(History::getId)
                .toList();

        Map<Long, String> historyImageMap = historyImageRepositoryService.findFirstImagesByHistoryIds(historyIds);

        return RecommendationConverter.toPeopleCacheDTO(recommendedMemberHistories, historyImageMap);
    }


    private List<Member> getFollowingMembers(Long memberId) {
        List<Member> followingMembers = followRepositoryService.findFollowingByFollowedId(memberId);

        if (followingMembers.isEmpty()) {
            return List.of();
        }

        return followingMembers.stream()
                .filter(filteredmember -> filteredmember.getVisibility().equals(Visibility.PUBLIC))
                .toList();
    }

    private String getHistoryImageUrl(Supplier<List<HashtagHistory>> historySupplier, Set<Long> blockingMembers) {
        List<HashtagHistory> histories = historySupplier.get();

        if (histories == null || histories.isEmpty()) {
            return null;
        }

        for (HashtagHistory history : histories) {
            if (history.getHistory() != null &&
                    history.getHistory().getVisibility() == Visibility.PUBLIC &&
                    !blockingMembers.contains(history.getHistory().getMember().getId())) {

                List<HistoryImage> images = historyImageRepositoryService.findByHistoryId(history.getHistory().getId());
                if (!images.isEmpty()) {
                    return images.get(0).getImageUrl();
                }
            }
        }

        return null;
    }

    public String getHistoryImageUrlByHashtagName(String hashtagName, Set<Long> blockingMembers) {
        return getHistoryImageUrl(() -> hashtagHistoryRepositoryService.findTop5HistoriesByHashtagNameOrderByDateDesc(hashtagName), blockingMembers);
    }

    public String getHistoryImageUrlByCategoryName(String categoryName, Set<Long> blockingMembers) {
        return getHistoryImageUrl(() -> hashtagHistoryRepositoryService.findTop5HistoriesByCategoryNameOrderByDateDesc(categoryName), blockingMembers);
    }


    @Override
    @Transactional(readOnly = true)
    public RecommendationResponseDTO.LastYearHistoryResult getLastYearHistory(Long memberId) {
        Member member = memberRepositoryService.findMemberById(memberId);
        LocalDate today = LocalDate.now();
        LocalDate oneYearAgo = today.minusYears(1);

        if (historyRepository.existsByHistoryDateAndMember_Id(oneYearAgo, memberId)) {
            History historyOneYearAgo = historyRepository.findByHistoryDateAndMember_Id(oneYearAgo, memberId).orElseThrow(()->new HistoryException(ErrorStatus.NO_SUCH_HISTORY));
            List<String> historyUrls = historyImageRepositoryService.findByHistoryId(historyOneYearAgo.getId()).stream()
                    .map(HistoryImage::getImageUrl)
                    .toList();
            return RecommendationConverter.toLastYearHistoryResult(historyOneYearAgo.getId(), historyOneYearAgo.getHistoryDate(), historyUrls, member, true);
        }

        List<Long> followingMembers = followRepositoryService.findFollowingByFollowedId(memberId).stream()
                .filter(member1 -> member1.getVisibility().equals(Visibility.PUBLIC))
                .map(Member::getId)
                .collect(Collectors.toList());

        List<Boolean> membersHaveHistoryOneYearAgo = historyRepository.existsByHistoryDateAndMemberIds(oneYearAgo, followingMembers);

        Long memberPicked = getRandomMemberWithHistory(followingMembers, membersHaveHistoryOneYearAgo);

        if (memberPicked != null) {
            History historyOneYearAgo = historyRepository.findByHistoryDateAndMember_Id(oneYearAgo, memberPicked).orElseThrow(()->new HistoryException(ErrorStatus.NO_SUCH_HISTORY));
            List<String> historyUrls = historyImageRepositoryService.findByHistoryId(historyOneYearAgo.getId()).stream()
                    .map(HistoryImage::getImageUrl)
                    .toList();
            return RecommendationConverter.toLastYearHistoryResult(historyOneYearAgo.getId(),historyOneYearAgo.getHistoryDate(), historyUrls, memberRepositoryService.findMemberById(memberPicked), false);
        }

        List<Long> members = new ArrayList<>(followingMembers);
        members.add(memberId);
        List<History> histories = historyRepository.findHistoriesByMemberIdsAndDateRange(members,oneYearAgo,today);
        if(histories != null && !histories.isEmpty()){
            History randomHistory = histories.get(new Random().nextInt(histories.size()));
            List<String> historyUrls = historyImageRepositoryService.findByHistoryId(randomHistory.getId()).stream()
                    .map(HistoryImage::getImageUrl)
                    .toList();
            return RecommendationConverter.toLastYearHistoryResult(randomHistory.getId(),randomHistory.getHistoryDate(),historyUrls,randomHistory.getMember(), randomHistory.getMember().equals(member));
        }

        return RecommendationConverter.toLastYearHistoryResult(null, null, null, member, null);
    }

    private Long getRandomMemberWithHistory(List<Long> followingMembers, List<Boolean> membersHaveHistoryOneYearAgo) {
        if (followingMembers == null || membersHaveHistoryOneYearAgo == null) {
            return null;
        }

        if (followingMembers.isEmpty() || membersHaveHistoryOneYearAgo.isEmpty()) {
            return null;
        }

        List<Long> candidates = new ArrayList<>();
        for (int i = 0; i < followingMembers.size(); i++) {
            if (Boolean.TRUE.equals(membersHaveHistoryOneYearAgo.get(i))) {
                candidates.add(followingMembers.get(i));
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }

        return candidates.get(new Random().nextInt(candidates.size()));
    }
}
