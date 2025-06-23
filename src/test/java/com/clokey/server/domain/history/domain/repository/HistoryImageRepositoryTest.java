package com.clokey.server.domain.history.domain.repository;

import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.history.domain.entity.HistoryImage;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.member.domain.repository.MemberRepository;
import com.clokey.server.domain.model.entity.enums.MemberStatus;
import com.clokey.server.domain.model.entity.enums.SocialType;
import com.clokey.server.domain.model.entity.enums.Visibility;
import com.clokey.server.global.config.QuerydslConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(QuerydslConfig.class)
class HistoryImageRepositoryTest {

    @Autowired
    HistoryImageRepository historyImageRepository;

    @Autowired
    HistoryRepository historyRepository;

    @Autowired
    MemberRepository memberRepository;

    @BeforeAll
    void setUp() {
        Member member1 = memberRepository.save(Member.builder().email("user1@example.com").nickname("User1").clokeyId("clokey1").bio("안녕하세요, User1입니다.").socialType(SocialType.KAKAO).profileImageUrl("https://example.com/user1.png").status(MemberStatus.ACTIVE).inactiveDate(null).visibility(Visibility.PUBLIC).build());

        History h1 = historyRepository.save(History.builder().historyDate(LocalDate.of(2025, 1, 1)).likes(5).visibility(Visibility.PUBLIC).content("새해 첫 기록입니다.").member(member1).banned(false).build());
        History h2 = historyRepository.save(History.builder().historyDate(LocalDate.of(2025, 1, 2)).likes(2).visibility(Visibility.PUBLIC).content("오늘은 책을 읽었습니다.").member(member1).banned(false).build());


        List<HistoryImage> h1Images = IntStream.range(1, 4)
                .mapToObj(i -> HistoryImage.builder()
                        .imageUrl("https://example.com/h1-img" + i + ".png")
                        .history(h1)
                        .build())
                .toList();

        List<HistoryImage> h2Images = IntStream.range(1, 4)
                .mapToObj(i -> HistoryImage.builder()
                        .imageUrl("https://example.com/h2-img" + i + ".png")
                        .history(h2)
                        .build())
                .toList();

        historyImageRepository.saveAll(h1Images);
        historyImageRepository.saveAll(h2Images);
    }

    @DisplayName("기록 Id를 기준으로 기록 사진 List를 반환할 수 있다")
    @Test
    void 기록의_사진_반환() {
        // given
        Long historyId = 1L;

        // when
        List<HistoryImage> historyImages = historyImageRepository.findByHistory_Id(historyId);

        // then
        assertThat(historyImages.stream()
                .map(HistoryImage::getId)
                .toList()).isEqualTo(List.of(1L, 2L, 3L));
    }

    @DisplayName("특정 기록의 사진을 만들어진 순서로 받아올 수 있다.")
    @Test
    void 특정_기록_사진_생성순() {
        // given
        Long historyId = 1L;

        // when
        List<String> urls = historyImageRepository.getImageUrlsByHistoryIdOrderByCreatedAtAsc(historyId);

        // then
        assertThat(urls).isEqualTo(List.of("https://example.com/h1-img1.png", "https://example.com/h1-img2.png", "https://example.com/h1-img3.png"));
    }

    @DisplayName("특정 기록의 사진을 모두 지운다.")
    @Test
    void 특정_기록의_사진_모두_지우기() {
        // given
        Long historyId = 1L;
        List<HistoryImage> historyImages = historyImageRepository.findByHistory_Id(historyId);

        assertThat(historyImages.size()).isEqualTo(3);

        // when
        historyImageRepository.deleteAllByHistoryIds(List.of(historyId));

        // then
        List<HistoryImage> newHistoryImages = historyImageRepository.findByHistory_Id(historyId);
        assertThat(newHistoryImages.size()).isEqualTo(0);
    }

    @DisplayName("특정 기록들의 historyImage를 모두 가져온다")
    @Test
    void 기록들의_사진_모두_가져오기() {
        // given
        List<Long> historyIds = List.of(1L, 2L);

        // when
        List<HistoryImage> images = historyImageRepository.findByHistoryIdIn(historyIds);

        // then
        Set<Long> imageIds = images.stream()
                .map(HistoryImage::getId)
                .collect(Collectors.toSet());
        assertThat(imageIds).isEqualTo(Set.of(1L, 2L, 3L, 4L, 5L, 6L));
    }

    @DisplayName("기록들의 첫 번째 사진(대표사진)을 가져온다")
    @Test
    void 기록들의_대표사진_가져오기() {
        // given
        List<Long> ids = List.of(1L, 2L);
        List<Object[]> answer = new ArrayList<>();
        answer.add(new Object[]{1L, "https://example.com/h1-img1.png"});
        answer.add(new Object[]{2L, "https://example.com/h2-img1.png"});

        // when
        List<Object[]> result = historyImageRepository.getFirstImageUrlsWithHistoryId(ids);

        // then
        assertThat(answer.stream()
                .map(Arrays::asList)
                .toList())
                .isEqualTo(result.stream()
                        .map(Arrays::asList)
                        .toList());
    }
}

