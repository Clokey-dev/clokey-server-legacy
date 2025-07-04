/*
package com.clokey.server.domain.history.domain.repository;

import com.clokey.server.domain.JpaIntegrationTestSupport;
import com.clokey.server.domain.history.domain.entity.Hashtag;
import com.clokey.server.domain.history.domain.entity.HashtagHistory;
import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.member.domain.repository.MemberRepository;
import com.clokey.server.domain.model.entity.enums.MemberStatus;
import com.clokey.server.domain.model.entity.enums.SocialType;
import com.clokey.server.domain.model.entity.enums.Visibility;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


class HashtagRepositoryTest extends JpaIntegrationTestSupport {

    @Autowired private MemberRepository memberRepository;
    @Autowired private HistoryRepository historyRepository;
    @Autowired private HashtagRepository hashtagRepository;
    @Autowired private HashtagHistoryRepository hashtagHistoryRepository;
*/
/*
    @BeforeAll
    void setup() {


        Member member1 = memberRepository.save(
                Member.builder()
                        .email("user1@example.com")
                        .nickname("User1")
                        .clokeyId("clokey1")
                        .bio("안녕하세요, User1입니다.")
                        .socialType(SocialType.APPLE)
                        .profileImageUrl("https://example.com/user1.png")
                        .status(MemberStatus.ACTIVE)
                        .visibility(Visibility.PUBLIC)
                        .build()
        );

        History history1 = historyRepository.save(
                History.builder()
                        .historyDate(LocalDate.of(2025, 1, 1))
                        .likes(5)
                        .visibility(Visibility.PUBLIC)
                        .content("새해 첫 기록입니다.")
                        .member(member1)
                        .banned(false)
                        .build()
        );

        History history2 = historyRepository.save(
                History.builder()
                        .historyDate(LocalDate.of(2025, 1, 2))
                        .likes(2)
                        .visibility(Visibility.PUBLIC)
                        .content("여행을 다녀왔어요.")
                        .member(member1)
                        .banned(false)
                        .build()
        );

        Hashtag tagSpring = hashtagRepository.save(
                Hashtag.builder().name("봄").build()
        );

        Hashtag tagTravel = hashtagRepository.save(
                Hashtag.builder().name("여행").build()
        );

        hashtagHistoryRepository.saveAll(List.of(
                HashtagHistory.builder().hashtag(tagSpring).history(history1).build(),
                HashtagHistory.builder().hashtag(tagTravel).history(history1).build(),
                HashtagHistory.builder().hashtag(tagTravel).history(history2).build()
        ));
    }*//*


    @DisplayName("특정 해시태그를 이름을 기준으로 조회가 가능합니다.")
    @Test
    void 특정_해시태그_이름_기준_조회() {

        // given & when
        Optional<Hashtag> hashtag = hashtagRepository.findByName("봄");

        // then
        assertThat(hashtag.get().getName()).isEqualTo("봄");
    }

    @DisplayName("다수의 해시태그를 이름으로 조회할 수 있다.")
    @Test
    void 해시태그들_이름_기준_조회() {

        // given & when
        List<Hashtag> hashtags = hashtagRepository.findHashtagsByNames(List.of("봄","여행"));

        // then
        assertThat(hashtags.size()).isEqualTo(2);
        assertThat(hashtags.get(0).getId()).isEqualTo(1L);
        assertThat(hashtags.get(1).getId()).isEqualTo(2L);
    }

    @DisplayName("존재하는 해시태그들의 존재 여부를 확인할 수 있다.")
    @ParameterizedTest(name = "hashtagName={0}, answer={1}")
    @CsvSource(
            value = {
                    "봄,true",
                    "여행,true",
                    "없는 예시1,false",
                    "없는 예시2,false"
            }
    )
    void 해시태그_존재_여부_확인(String hashtagName, boolean answer) {

        // given & when
        boolean exists = hashtagRepository.existsByName(hashtagName);

        // then
        assertThat(exists).isEqualTo(answer);
    }

}
*/
