package com.clokey.server.domain.history.domain.repository;

import com.clokey.server.domain.history.domain.entity.Hashtag;
import com.clokey.server.domain.history.domain.entity.HashtagHistory;
import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.member.domain.repository.MemberRepository;
import com.clokey.server.domain.model.entity.enums.MemberStatus;
import com.clokey.server.domain.model.entity.enums.SocialType;
import com.clokey.server.domain.model.entity.enums.Visibility;
import com.clokey.server.global.config.QuerydslConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@DataJpaTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(QuerydslConfig.class)
class HashtagHistoryRepositoryTest {


    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private HashtagRepository hashtagRepository;
    @Autowired
    private HashtagHistoryRepository hashtagHistoryRepository;

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
    }

    @DisplayName("기록의 ID를 기준으로 HashtagHistory를 모두 가져옵니다")
    @Test
    void 특정_기록에_기록된_해시태그_기록_조회() {

        // given & when
        List<HashtagHistory> hashtags = hashtagHistoryRepository.findByHistory_Id(1L);

        // then
        assertThat(hashtags.size()).isEqualTo(2);
        assertThat(hashtags.get(0).getHashtag().getName()).isEqualTo("봄");
        assertThat(hashtags.get(1).getHashtag().getName()).isEqualTo("여행");
    }

    @DisplayName("기록의 ID를 기준으로 Hashtag 이름들을 모두 가져옵니다")
    @Test
    void 특정_기록에_등록된_해시태그_이름_조회() {

        // given & when
        List<String> hashtags = hashtagHistoryRepository.findHashtagNamesByHistoryId(1L);

        // then
        assertThat(hashtags.size()).isEqualTo(2);
        assertThat(hashtags.get(0)).isEqualTo("봄");
        assertThat(hashtags.get(1)).isEqualTo("여행");
    }
}
