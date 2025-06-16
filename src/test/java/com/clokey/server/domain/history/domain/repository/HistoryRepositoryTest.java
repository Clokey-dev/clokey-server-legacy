package com.clokey.server.domain.history.domain.repository;

import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.history.domain.entity.MemberLike;
import com.clokey.server.domain.history.dto.projection.HistoryProjectionDTO;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.member.domain.repository.MemberRepository;
import com.clokey.server.domain.model.entity.enums.MemberStatus;
import com.clokey.server.domain.model.entity.enums.RegisterStatus;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(QuerydslConfig.class)
class HistoryRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private MemberLikeRepository memberLikeRepository;

    @BeforeAll
    void setup() {
        Member member1 = Member.builder()
                .email("user1@example.com")
                .nickname("UserOne")
                .clokeyId("clokey1")
                .socialType(SocialType.KAKAO)
                .status(MemberStatus.ACTIVE)
                .registerStatus(RegisterStatus.REGISTERED)
                .visibility(Visibility.PUBLIC)
                .banned(false)
                .build();

        Member member2 = Member.builder()
                .email("user2@example.com")
                .nickname("UserTwo")
                .clokeyId("clokey2")
                .socialType(SocialType.APPLE)
                .status(MemberStatus.INACTIVE)
                .registerStatus(RegisterStatus.REGISTERED)
                .visibility(Visibility.PRIVATE)
                .banned(false)
                .build();

        Member member3 = Member.builder()
                .email("user3@example.com")
                .nickname("UserThree")
                .clokeyId("clokey3")
                .socialType(SocialType.KAKAO)
                .status(MemberStatus.ACTIVE)
                .registerStatus(RegisterStatus.NOT_AGREED)
                .visibility(Visibility.PUBLIC)
                .banned(false)
                .build();

        memberRepository.saveAll(List.of(member1,member2,member3));

        // === Member 1 Histories ===
        historyRepository.save(History.builder()
                .historyDate(LocalDate.of(2024, 6, 1))
                .likes(5)
                .visibility(Visibility.PUBLIC)
                .content("공개된 히스토리 - member1")
                .banned(false)
                .member(member1)
                .build());

        historyRepository.save(History.builder()
                .historyDate(LocalDate.of(2024, 6, 3))
                .likes(3)
                .visibility(Visibility.PRIVATE)
                .content("비공개 히스토리 - member1")
                .banned(false)
                .member(member1)
                .build());

        historyRepository.save(History.builder()
                .historyDate(LocalDate.of(2024, 6, 5))
                .likes(7)
                .visibility(Visibility.PUBLIC)
                .content("공개 히스토리 2 - member1")
                .banned(false)
                .member(member1)
                .build());

        historyRepository.save(History.builder()
                .historyDate(LocalDate.of(2024, 6, 7))
                .likes(10)
                .visibility(Visibility.PRIVATE)
                .content("비공개 히스토리 2 - member1")
                .banned(false)
                .member(member1)
                .build());

        // === Member 2 Histories ===
        History m2h1 = historyRepository.save(History.builder()
                .historyDate(LocalDate.of(2024, 5, 10))
                .likes(2)
                .visibility(Visibility.PUBLIC)
                .content("공개 히스토리 - member2")
                .banned(false)
                .member(member2)
                .build());

        historyRepository.save(History.builder()
                .historyDate(LocalDate.of(2024, 5, 12))
                .likes(1)
                .visibility(Visibility.PRIVATE)
                .content("비공개 히스토리 - member2")
                .banned(false)
                .member(member2)
                .build());

        historyRepository.save(History.builder()
                .historyDate(LocalDate.of(2024, 5, 14))
                .likes(4)
                .visibility(Visibility.PUBLIC)
                .content("공개 히스토리 2 - member2")
                .banned(false)
                .member(member2)
                .build());

        historyRepository.save(History.builder()
                .historyDate(LocalDate.of(2024, 5, 16))
                .likes(0)
                .visibility(Visibility.PRIVATE)
                .content("비공개 히스토리 2 - member2")
                .banned(false)
                .member(member2)
                .build());

        // === Member 3 Histories ===
        History m3h1 = historyRepository.save(History.builder()
                .historyDate(LocalDate.of(2024, 4, 5))
                .likes(9)
                .visibility(Visibility.PUBLIC)
                .content("공개 히스토리 - member3")
                .banned(false)
                .member(member3)
                .build());

        historyRepository.save(History.builder()
                .historyDate(LocalDate.of(2024, 4, 6))
                .likes(2)
                .visibility(Visibility.PRIVATE)
                .content("비공개 히스토리 - member3")
                .banned(false)
                .member(member3)
                .build());

        historyRepository.save(History.builder()
                .historyDate(LocalDate.of(2024, 4, 7))
                .likes(6)
                .visibility(Visibility.PUBLIC)
                .content("공개 히스토리 2 - member3")
                .banned(false)
                .member(member3)
                .build());

        historyRepository.save(History.builder()
                .historyDate(LocalDate.of(2024, 4, 8))
                .likes(0)
                .visibility(Visibility.PRIVATE)
                .content("비공개 히스토리 2 - member3")
                .banned(false)
                .member(member3)
                .build());

        // === MemberLikes (user1이 user2, user3의 게시물 좋아요) ===
        memberLikeRepository.save(MemberLike.builder()
                .member(member1)
                .history(m2h1)
                .build());

        memberLikeRepository.save(MemberLike.builder()
                .member(member1)
                .history(m3h1)
                .build());
    }

    //1번 Member의 월별 기록
    @DisplayName("월별 기록을 정확하게 조회할 수 있다.")
    @Test
    void 월별_기록_조회() {
        // given
        Long memberId = 1L;
        String yearMonth = "2024-06";

        // when
        List<HistoryProjectionDTO> monthlyHistoryProjectionDTOS = historyRepository.getMonthlyHistoriesByMemberAndYearMonth(memberId,yearMonth);

        // then
        assertThat(monthlyHistoryProjectionDTOS.size()).isEqualTo(4);

        assertThat(monthlyHistoryProjectionDTOS.stream()
                .map(HistoryProjectionDTO::getId)
                .toList()).isEqualTo(List.of(1L,2L,3L,4L));

        assertThat(monthlyHistoryProjectionDTOS.stream()
                .map(HistoryProjectionDTO::getHistoryDate)
                .toList()).isEqualTo(List.of(
                LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 6, 3),
                LocalDate.of(2024, 6, 5),
                LocalDate.of(2024, 6, 7)
        ));

        assertThat(monthlyHistoryProjectionDTOS.stream()
                .map(HistoryProjectionDTO::getVisibility)
                .toList()).isEqualTo(List.of(
                Visibility.PUBLIC,
                Visibility.PRIVATE,
                Visibility.PUBLIC,
                Visibility.PRIVATE
        ));
    }





}
