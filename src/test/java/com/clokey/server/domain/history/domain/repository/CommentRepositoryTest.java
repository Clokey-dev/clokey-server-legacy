package com.clokey.server.domain.history.domain.repository;

import com.clokey.server.domain.history.domain.entity.Comment;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(QuerydslConfig.class)
class CommentRepositoryTest {

    @Autowired private MemberRepository memberRepository;
    @Autowired private HistoryRepository historyRepository;
    @Autowired private CommentRepository commentRepository;

    @BeforeAll
    void setup() {

        Member member1 = memberRepository.save(Member.builder()
                .email("user1@example.com")
                .nickname("User1")
                .clokeyId("clokey1")
                .bio("첫 번째 사용자")
                .profileImageUrl("https://example.com/user1.png")
                .socialType(SocialType.KAKAO)
                .status(MemberStatus.ACTIVE)
                .visibility(Visibility.PUBLIC)
                .build());

        Member member2 = memberRepository.save(Member.builder()
                .email("user2@example.com")
                .nickname("User2")
                .clokeyId("clokey2")
                .bio("두 번째 사용자")
                .profileImageUrl("https://example.com/user2.png")
                .socialType(SocialType.APPLE)
                .status(MemberStatus.ACTIVE)
                .visibility(Visibility.PUBLIC)
                .build());

        Member member3 = memberRepository.save(Member.builder()
                .email("user3@example.com")
                .nickname("User3")
                .clokeyId("clokey3")
                .bio("세 번째 사용자")
                .profileImageUrl("https://example.com/user3.png")
                .socialType(SocialType.KAKAO)
                .status(MemberStatus.ACTIVE)
                .visibility(Visibility.PUBLIC)
                .build());

        History history1 = historyRepository.save(History.builder()
                .historyDate(LocalDate.of(2025, 1, 1))
                .likes(3)
                .visibility(Visibility.PUBLIC)
                .content("새해 첫 기록")
                .member(member1)
                .banned(false)
                .build());

        History history2 = historyRepository.save(History.builder()
                .historyDate(LocalDate.of(2025, 1, 2))
                .likes(1)
                .visibility(Visibility.PUBLIC)
                .content("두 번째 기록")
                .member(member2)
                .banned(false)
                .build());


        Comment root1 = commentRepository.save(Comment.builder()
                .content("좋은 글이네요!")
                .member(member2)
                .history(history1)
                .build());

        Comment root2 = commentRepository.save(Comment.builder()
                .content("정말 공감합니다.")
                .member(member3)
                .history(history1)
                .build());

        Comment root3 = commentRepository.save(Comment.builder()
                .content("저도 비슷한 경험이 있어요.")
                .member(member1)
                .history(history2)
                .banned(true)
                .build());


        Comment reply1 = commentRepository.save(Comment.builder()
                .content("감사합니다!")
                .member(member1)
                .history(history1)
                .comment(root1)
                .build());

        Comment reply2 = commentRepository.save(Comment.builder()
                .content("맞아요. 저도 공감돼요.")
                .member(member2)
                .history(history1)
                .comment(root2)
                .build());

        Comment reply3 = commentRepository.save(Comment.builder()
                .content("혹시 어떤 경험이셨는지 공유해 주실 수 있나요?")
                .member(member3)
                .history(history2)
                .comment(root3)
                .build());

        Comment reply4 = commentRepository.save(Comment.builder()
                .content("댓글이 도움이 되었어요.")
                .member(member1)
                .history(history2)
                .comment(root3)
                .banned(true)
                .build());
    }

    @DisplayName("특정 기록에 신고 당하지 않은 root 댓글 개수를 반환합니다")
    @Test
    void 신고당하지_않은_root_댓글_조회(){

        // given & when
        int count = commentRepository.countActiveRootComments(1L);

        // then
        assertThat(count).isEqualTo(2);
    }

    @DisplayName("특정 댓글의 소유권을 확인합니다")
    @ParameterizedTest(name = "commentId={0}, memberId={1}, expected={2}")
    @CsvSource(
            value = {
                    "1,2,true",
                    "2,3,true",
                    "10,1,false",
                    "10,2,false"
            }
    )
    void 특정_댓글의_소유권_확인(Long commentId, Long memberId, boolean expected){

        // given & when
        boolean answer = commentRepository.existsByIdAndMemberId(commentId,memberId);

        // then
        assertThat(answer).isEqualTo(expected);
    }

    @DisplayName("특정 댓글이 특정 기록의 댓글인지 확인합니다")
    @ParameterizedTest(name = "commentId={0}, historyId={1}, expected={2}")
    @CsvSource(
            value = {
                    "1,1,true",
                    "2,1,true",
                    "3,10,false",
                    "4,10,false"
            }
    )
    void 특정_댓글이_특정_기록의_댓글(Long commentId,Long historyId, boolean expected){

        // given & when
        boolean answer = commentRepository.existsByIdAndHistoryId(commentId,historyId);

        // then
        assertThat(answer).isEqualTo(expected);
    }

    @DisplayName("특정 기록의 댓글수를 반환합니다")
    @ParameterizedTest(name = "historyId={0},  expected={1}")
    @CsvSource(
            value = {
                    "1,4",
                    "2,3",
            }
    )
    void 특정_기록의_댓글수_조회(Long historyId, Long expected){

        // given & when
        Long answer = commentRepository.countByHistoryId(historyId);

        // then
        assertThat(answer).isEqualTo(expected);
    }

    @DisplayName("특정 유저가 작성한 댓글을 history와 함께 가져옵니다.")
    @Test
    void 작성_댓글_기록_JOIN() {
        // given
        Long memberId = 1L;

        // when
        Page<Comment> comments = commentRepository.findByMember_Id(memberId,Pageable.ofSize(10));

        // then
        assertThat(comments.getTotalElements()).isEqualTo(3);
    }

}

