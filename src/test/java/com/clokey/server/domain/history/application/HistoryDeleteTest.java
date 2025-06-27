package com.clokey.server.domain.history.application;

import com.clokey.server.domain.cloth.domain.repository.ClothRepository;
import com.clokey.server.domain.history.api.HistoryRestController;
import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.history.domain.repository.*;
import com.clokey.server.domain.history.exception.HistoryException;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.member.domain.repository.MemberRepository;
import com.clokey.server.domain.search.application.SearchRepositoryService;
import com.clokey.server.global.error.code.status.ErrorStatus;
import jakarta.validation.ConstraintViolationException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("local")
public class HistoryDeleteTest {

    @Autowired
    private HistoryService historyService;

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private HistoryClothRepository historyClothRepository;

    @Autowired
    private MemberLikeRepository memberLikeRepository;

    @Autowired
    private HistoryImageRepository historyImageRepository;

    @Autowired
    private SearchRepositoryService searchRepositoryService;

    @Autowired
    private HistoryRestController historyRestController;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ClothRepository clothRepository;

    /**
     * 기록 삭제 API TEST
     */

    @DisplayName("기록을 삭제하며 관련된 모든 것들을 삭제할 수 있다.")
    @Test
    @Order(1)
    void 기록_삭제_성공1() throws IOException {

        //given
        History history = historyRepository.findById(1L).get();

        // 부모 댓글과 대댓글이 존재한다
        assertThat(commentRepository.existsByHistoryIdAndCommentIsNull(1L)).isTrue();
        assertThat(commentRepository.existsByHistoryIdAndCommentIsNotNull(1L)).isTrue();

        // 옷과 기록의 매핑 테이블에 데이터가 존재한다
        assertThat(historyClothRepository.existsByHistoryId(1L)).isTrue();

        // 좋아요 기록을 확인한다.
        assertThat(memberLikeRepository.existsByHistoryId(1L)).isTrue();

        // 기록과 관련된 사진이 존재합니다.
        assertThat(historyImageRepository.existsByHistoryId(1L)).isTrue();

        //옷의 착용 횟수 확인 (삭제전)
        assertThat(clothRepository.findById(1L).get().getWearNum()).isEqualTo(6);

        // when
        // 1번 Member의 1번 기록을 지웁니다.
        historyService.deleteHistory(1L, 1L);

        // then
        // 부모 댓글과 대댓글이 존재하지 않는다
        assertThat(commentRepository.existsByHistoryIdAndCommentIsNull(1L)).isFalse();
        assertThat(commentRepository.existsByHistoryIdAndCommentIsNotNull(1L)).isFalse();

        // 옷과 기록의 매핑 테이블에 데이터가 존재한다
        assertThat(historyClothRepository.existsByHistoryId(1L)).isFalse();

        // 좋아요 기록을 확인한다.
        assertThat(memberLikeRepository.existsByHistoryId(1L)).isFalse();

        // 기록과 관련된 사진이 존재합니다.
        assertThat(historyImageRepository.existsByHistoryId(1L)).isFalse();

        //옷의 착용 횟수 확인 (삭제 후)
        assertThat(clothRepository.findById(1L).get().getWearNum()).isEqualTo(5);

    }

    @DisplayName("ES 복구")
    @Test
    @Order(2)
    void ES_Recovery() throws IOException {

        History history = historyRepository.findById(1L).get();

        searchRepositoryService.updateHistoryDataToElasticsearch(history);
    }

    @DisplayName("존재하지 않는 historyId를 입력할 경우 Controller단에서 에러가 발생한다.")
    @ParameterizedTest
    @ValueSource(longs = {100L, 2000L, 1234L})
    void 기록_삭제_예외1(Long historyId) {

        // given
        Member member = memberRepository.findById(1L).get();

        // then
        assertThatThrownBy(() -> historyRestController.deleteHistory(member, historyId))
                .isInstanceOfSatisfying(ConstraintViolationException.class, ex ->
                        Assertions.assertThat(ex.getMessage()).contains(ErrorStatus.NO_SUCH_HISTORY.name())
                );
    }

    @DisplayName("나의 기록이 아닌 기록을 삭제하려는 경우 Service단에서 에러가 발생합니다.")
    @Test
    void 기록_삭제_예외2() {

        // given
        // 거짓 : 1번 history는 1번 Member의 기록입니다.
        Member member = memberRepository.findById(2L).get();
        Long historyId = 1L;

        // then
        assertThatThrownBy(() -> historyRestController.deleteHistory(member, historyId))
                .isInstanceOfSatisfying(HistoryException.class, ex ->
                        Assertions.assertThat(ex.getCode()).isEqualTo(ErrorStatus.NOT_MY_HISTORY)
                );
    }

    @DisplayName("댓글을 지우고 대댓글도 모두 삭제됩니다.")
    @Test
    void 댓글_삭제_성공1() {

        //given
        Long targetCommentId = 1L;
        Long commentWriterId = 1L;

        // 1번 댓글이 존재하고 1번 댓글에는 대댓글이 달려있다.
        assertThat(commentRepository.existsById(1L)).isTrue();
        assertThat(commentRepository.existsByComment_Id(1L)).isTrue();

        // when
        historyService.deleteComment(targetCommentId, commentWriterId);

        // then
        // 댓글과 대댓글이 모두 삭제됨.
        assertThat(commentRepository.existsById(1L)).isFalse();
        assertThat(commentRepository.existsByComment_Id(1L)).isFalse();
    }

    @DisplayName("본인이 작성하지 않은 댓글을 삭제 시도할 경우 Service단에서 에러가납니다.")
    @Test
    void 댓글_삭제_예외1() {

        //given
        // 거짓 : 1번 댓글은 1번 Member가 작성하였습니다.
        Long targetCommentId = 1L;
        Long commentWriterId = 2L;

        // then
        assertThatThrownBy(() -> historyService.deleteComment(targetCommentId, commentWriterId))
                .isInstanceOfSatisfying(HistoryException.class, ex ->
                        Assertions.assertThat(ex.getCode()).isEqualTo(ErrorStatus.NOT_MY_COMMENT)
                );
    }

}
