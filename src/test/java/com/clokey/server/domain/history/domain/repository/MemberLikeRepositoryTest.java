package com.clokey.server.domain.history.domain.repository;

import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.history.domain.entity.MemberLike;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.member.domain.repository.MemberRepository;
import com.clokey.server.domain.model.entity.enums.MemberStatus;
import com.clokey.server.domain.model.entity.enums.SocialType;
import com.clokey.server.domain.model.entity.enums.Visibility;
import com.clokey.server.global.config.QuerydslConfig;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
class MemberLikeRepositoryTest {

    @Autowired
    MemberLikeRepository memberLikeRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    HistoryRepository historyRepository;

    @BeforeAll
    void setUp() {
        Member member1 = memberRepository.save(Member.builder().email("user1@example.com").nickname("User1").clokeyId("clokey1").bio("안녕하세요, User1입니다.").socialType(SocialType.KAKAO).profileImageUrl("https://example.com/user1.png").status(MemberStatus.ACTIVE).inactiveDate(null).visibility(Visibility.PUBLIC).build());
        Member member2 = memberRepository.save(Member.builder().email("user2@example.com").nickname("User2").clokeyId("clokey2").bio("여행을 좋아하는 User2입니다.").socialType(SocialType.KAKAO).profileImageUrl("https://example.com/user2.png").status(MemberStatus.ACTIVE).visibility(Visibility.PRIVATE).build());
        Member member3 = memberRepository.save(Member.builder().email("user3@example.com").nickname("User3").clokeyId("clokey3").bio("개발자가 되고 싶은 User3입니다.").socialType(SocialType.APPLE).profileImageUrl("https://example.com/user3.png").status(MemberStatus.INACTIVE).inactiveDate(LocalDate.now()).visibility(Visibility.PUBLIC).build());

        History h1 = historyRepository.save(History.builder().historyDate(LocalDate.of(2025, 1, 1)).likes(2).visibility(Visibility.PUBLIC).content("새해 첫 기록입니다.").member(member1).banned(false).build());
        History h2 = historyRepository.save(History.builder().historyDate(LocalDate.of(2025, 1, 2)).likes(1).visibility(Visibility.PUBLIC).content("오늘은 책을 읽었습니다.").member(member1).banned(false).build());
        History h3 = historyRepository.save(History.builder().historyDate(LocalDate.of(2025, 1, 1)).likes(1).visibility(Visibility.PRIVATE).content("새해를 맞아 여행을 다녀왔습니다.").member(member2).banned(false).build());
        History h4 = historyRepository.save(History.builder().historyDate(LocalDate.of(2025, 1, 3)).likes(0).visibility(Visibility.PUBLIC).content("맛집을 다녀왔어요!").member(member2).banned(false).build());
        History h5 = historyRepository.save(History.builder().historyDate(LocalDate.of(2024, 12, 25)).likes(0).visibility(Visibility.PUBLIC).content("크리스마스에 쉬는 날을 보냈습니다.").member(member3).banned(false).build());
        History h6 = historyRepository.save(History.builder().historyDate(LocalDate.of(2025, 1, 5)).likes(0).visibility(Visibility.PRIVATE).content("새해 목표를 세웠습니다.").member(member3).banned(false).build());

        memberLikeRepository.save(MemberLike.builder().member(member1).history(h1).build());
        memberLikeRepository.save(MemberLike.builder().member(member1).history(h2).build());
        memberLikeRepository.save(MemberLike.builder().member(member2).history(h1).build());
        memberLikeRepository.save(MemberLike.builder().member(member3).history(h3).build());
    }

    @DisplayName("특정 회원이 특정 기록에 좋아요를 눌렀는지 확인할 수 있다.")
    @ParameterizedTest(name = "historyId={0}, memberId={1}, answer={2}")
    @CsvSource(
            value = {
                    "1,1,true",
                    "2,1,true",
                    "3,1,false",
                    "4,1,false"
            }
    )
    void 특정_회원_특정_기록_좋아요_확인(Long historyId, Long memberId, boolean answer) {
        // given & when
        boolean result = memberLikeRepository.existsByMemberIdAndHistoryId(memberId, historyId);

        // then
        assertThat(result).isEqualTo(answer);
    }

    @DisplayName("특정 회원이 특정 기록에 좋아요를 누른 기록을 삭제할 수 있다.")
    @Test
    void 특정_회원의_특정_기록_좋아요_삭제() {
        // given
        Long memberId = 1L;
        Long historyId = 1L;

        boolean existCheck = memberLikeRepository.existsByMemberIdAndHistoryId(memberId, historyId);
        assertThat(existCheck).isTrue();

        // when
        memberLikeRepository.deleteByMemberIdAndHistoryId(memberId, historyId);

        // then
        boolean newExistCheck = memberLikeRepository.existsByMemberIdAndHistoryId(memberId, historyId);
        assertThat(newExistCheck).isFalse();
    }

    @DisplayName("특정 기록과 관련된 모든 좋아요를 삭제한다.")
    @Test
    void 특정_기록_좋아요_모두_삭제() {
        // given
        Long historyId = 1L;
        boolean existCheck = memberLikeRepository.existsByHistoryId(historyId);
        assertThat(existCheck).isTrue();

        // when
        memberLikeRepository.deleteByHistoryId(historyId);

        // then
        boolean newExistCheck = memberLikeRepository.existsByHistoryId(historyId);
        assertThat(newExistCheck).isFalse();
    }

    @DisplayName("특정 회원과 관련된 모든 좋아요를 삭제한다.")
    @Test
    void 특정_회원_좋아요_모두_삭제() {
        // given
        Long memberId = 1L;

        boolean existCheck = memberLikeRepository.existsByMemberId(memberId);
        assertThat(existCheck).isTrue();

        // when
        memberLikeRepository.deleteAllByMemberId(memberId);

        // then
        boolean newExistCheck = memberLikeRepository.existsByMemberId(memberId);
        assertThat(newExistCheck).isFalse();
    }

    @DisplayName("기록들의 좋아요를 모두 삭제한다.")
    @Test
    void 기록들의_좋아요_모두_삭제() {
        // given
        List<Long> ids = List.of(1L, 2L);
        boolean existCheck1 = memberLikeRepository.existsByHistoryId(1L);
        boolean existCheck2 = memberLikeRepository.existsByHistoryId(2L);

        assertThat(existCheck1).isTrue();
        assertThat(existCheck2).isTrue();

        // when
        memberLikeRepository.deleteAllByHistoryIds(ids);

        // then
        boolean newExistCheck1 = memberLikeRepository.existsByHistoryId(1L);
        boolean newExistCheck2 = memberLikeRepository.existsByHistoryId(2L);

        assertThat(newExistCheck1).isFalse();
        assertThat(newExistCheck2).isFalse();

    }

}
