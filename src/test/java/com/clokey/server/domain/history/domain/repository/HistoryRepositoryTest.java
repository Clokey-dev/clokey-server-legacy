package com.clokey.server.domain.history.domain.repository;

import com.clokey.server.domain.history.domain.entity.History;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(QuerydslConfig.class)
public class HistoryRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private HistoryRepository historyRepository;

    @BeforeAll
    void setUp() {
        Member member1 = memberRepository.save(Member.builder().email("user1@example.com").nickname("User1").clokeyId("clokey1").bio("안녕하세요, User1입니다.").socialType(SocialType.KAKAO).profileImageUrl("https://example.com/user1.png").status(MemberStatus.ACTIVE).inactiveDate(null).visibility(Visibility.PUBLIC).build());
        Member member2 = memberRepository.save(Member.builder().email("user2@example.com").nickname("User2").clokeyId("clokey2").bio("여행을 좋아하는 User2입니다.").socialType(SocialType.KAKAO).profileImageUrl("https://example.com/user2.png").status(MemberStatus.ACTIVE).visibility(Visibility.PRIVATE).build());
        Member member3 = memberRepository.save(Member.builder().email("user3@example.com").nickname("User3").clokeyId("clokey3").bio("개발자가 되고 싶은 User3입니다.").socialType(SocialType.APPLE).profileImageUrl("https://example.com/user3.png").status(MemberStatus.INACTIVE).inactiveDate(LocalDate.now()).visibility(Visibility.PUBLIC).build());
        Member member4 = memberRepository.save(Member.builder().email("user4@example.com").nickname("User4").clokeyId("clokey4").bio("영화를 좋아하는 User4입니다.").socialType(SocialType.APPLE).profileImageUrl("https://example.com/user4.png").status(MemberStatus.ACTIVE).visibility(Visibility.PUBLIC).build());
        Member member5 = memberRepository.save(Member.builder().email("user5@example.com").nickname("User5").clokeyId("clokey5").bio("독서를 즐기는 User5입니다.").socialType(SocialType.KAKAO).profileImageUrl("https://example.com/user5.png").status(MemberStatus.INACTIVE).inactiveDate(LocalDate.now()).visibility(Visibility.PRIVATE).build());

        History h1 = historyRepository.save(History.builder().historyDate(LocalDate.of(2025, 1, 1)).likes(5).visibility(Visibility.PUBLIC).content("새해 첫 기록입니다.").member(member1).banned(false).build());
        History h2 = historyRepository.save(History.builder().historyDate(LocalDate.of(2025, 1, 2)).likes(2).visibility(Visibility.PUBLIC).content("오늘은 책을 읽었습니다.").member(member1).banned(false).build());
        History h3 = historyRepository.save(History.builder().historyDate(LocalDate.of(2025, 1, 1)).likes(1).visibility(Visibility.PRIVATE).content("새해를 맞아 여행을 다녀왔습니다.").member(member2).banned(false).build());
        History h4 = historyRepository.save(History.builder().historyDate(LocalDate.of(2025, 1, 3)).likes(3).visibility(Visibility.PUBLIC).content("맛집을 다녀왔어요!").member(member2).banned(false).build());
        History h5 = historyRepository.save(History.builder().historyDate(LocalDate.of(2024, 12, 25)).likes(0).visibility(Visibility.PUBLIC).content("크리스마스에 쉬는 날을 보냈습니다.").member(member3).banned(false).build());
        History h6 = historyRepository.save(History.builder().historyDate(LocalDate.of(2025, 1, 5)).likes(1).visibility(Visibility.PRIVATE).content("새해 목표를 세웠습니다.").member(member3).banned(false).build());
        History h7 = historyRepository.save(History.builder().historyDate(LocalDate.of(2025, 1, 1)).likes(7).visibility(Visibility.PUBLIC).content("영화를 한 편 봤어요.").member(member4).banned(false).build());
        History h8 = historyRepository.save(History.builder().historyDate(LocalDate.of(2025, 1, 6)).likes(4).visibility(Visibility.PRIVATE).content("운동을 시작했습니다.").member(member4).banned(false).build());
        History h9 = historyRepository.save(History.builder().historyDate(LocalDate.of(2025, 1, 3)).likes(2).visibility(Visibility.PRIVATE).content("오늘은 도서관에서 시간을 보냈습니다.").member(member5).banned(false).build());
        History h10 = historyRepository.save(History.builder().historyDate(LocalDate.of(2025, 1, 4)).likes(1).visibility(Visibility.PUBLIC).content("독서 클럽에 참여했습니다.").member(member5).banned(false).build());
    }

    @DisplayName("특정 유저의 특정 날짜 이후에 기록을 모두 반환한다.")
    @Test
    void 특정_유저_특정_날짜_이후_기록() {
        // given
        Long memberId = 1L;
        LocalDate date = LocalDate.of(2025, 1, 2);

        // when
        List<History> result = historyRepository.findHistoriesWithinMonth(memberId, date);

        // then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getId()).isEqualTo(2L);
    }

    @DisplayName("특정 날짜에 유저의 기록이 존재하는지 반환한다.")
    @ParameterizedTest(name = "year={0}, month={1}, day={2}, memberId={3}, exist={4}")
    @CsvSource(
            value = {
                    "2025, 1, 1, 1, true",
                    "2025, 1, 1, 2, true",
                    "2080, 1, 1, 1, false",
                    "2090, 1, 1, 1, false"
            }
    )
    void 특정_유저_특정_날짜_이후_기록(int year, int month, int day, Long memberId, boolean exist) {

        // given & when
        boolean result = historyRepository.existsByHistoryDateAndMember_Id(LocalDate.of(year, month, day), memberId);

        // then
        assertThat(result).isEqualTo(exist);
    }

    @DisplayName("특정 유저의 Id들과 날짜를 받았을 경우 존재하는지 알려준다.")
    @Test
    void 특정_유저들_특정_날짜들_기록_존재_여부() {
        // given
        LocalDate date = LocalDate.of(2025, 1, 1);
        List<Long> memberIds = List.of(1L, 2L, 3L);

        // when
        List<Boolean> result = historyRepository.existsByHistoryDateAndMemberIds(date, memberIds);

        // then
        assertThat(result).isEqualTo(List.of(true, true, false));

    }

    @DisplayName("나의 history를 판별해서 반환한다.")
    @ParameterizedTest(name = "historyId={0}, memberId={1}, isMine={2}")
    @CsvSource(
            value = {
                    "1,1,true",
                    "2,1,true",
                    "3,1,false",
                    "4,1,false"
            }
    )
    void 나의_기록_판별(Long historyId, Long memberId, boolean isMine) {
        // given & when
        boolean result = historyRepository.checkMyHistory(historyId, memberId);

        // then
        assertThat(result).isEqualTo(isMine);
    }

    @DisplayName("유저의 Id와 기록의 날짜로 기록을 찾아낸다")
    @Test
    void 유저_Id_기록_날짜_찿기() {
        // given
        LocalDate existingDate = LocalDate.of(2025, 1, 1);
        LocalDate nonExistingDate = LocalDate.of(2500, 1, 1);
        Long memberId = 1L;

        // when
        Optional<History> existingHistory = historyRepository.findByHistoryDateAndMember_Id(existingDate, memberId);
        Optional<History> nonExistingHistory = historyRepository.findByHistoryDateAndMember_Id(nonExistingDate, memberId);

        // then
        assertThat(existingHistory.get().getId()).isEqualTo(1L);
        assertThat(nonExistingHistory.isPresent()).isEqualTo(false);
    }

    @DisplayName("특정 유저의 기록 수를 반환할 수 있다.")
    @ParameterizedTest(name = "memberId={0}, historyCount={1}")
    @CsvSource(
            value = {
                    "1,2",
                    "2,2",
                    "3,2",
                    "4,2"
            }
    )
    void 특정_유저_기록수_반환(Long memberId, Long historyCount) {
        // given
        Member member = memberRepository.findById(memberId).get();

        // when
        Long result = historyRepository.countHistoryByMember(member);

        // then
        assertThat(result).isEqualTo(historyCount);
    }

    @DisplayName("기록을 가져올 수 있다")
    @Test
    void 기록_가져오기() {
        // given
        Long historyId = 1L;

        // when
        Optional<History> history = historyRepository.findByIdWithWriter(historyId);

        // then
        assertThat(history.isPresent()).isTrue();
        assertThat(history.get().getId()).isEqualTo(historyId);
    }

}

