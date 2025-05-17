package com.clokey.server.domain.history;


import com.clokey.server.domain.history.api.HistoryRestController;
import com.clokey.server.domain.history.application.HistoryService;
import com.clokey.server.domain.history.dto.HistoryResponseDTO;
import com.clokey.server.domain.history.exception.HistoryException;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.member.domain.repository.MemberRepository;
import com.clokey.server.global.error.code.status.ErrorStatus;
import com.clokey.server.global.error.exception.DatabaseException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("local")
class HistoryReadTest {

    @Autowired
    private HistoryService historyService;

    @Autowired
    private HistoryRestController historyRestController;

    @Autowired
    private MemberRepository memberRepository;

    /*월별 기록 조회 TEST*/

    @DisplayName("나와 타인의 월별 기록의 개수를 정확하게 반환한다.")
    @ParameterizedTest(name = "clokeyId={0}, 기대 기록 수={1}, 조회 월={2}")
    @CsvSource(
            nullValues = "null",
            value = {
                    "null, 2, 2025-01",     // 자기 자신
                    "clokey3, 1, 2024-12"   // 타인
            }
    )
    void 월별_기록_조회_성공_1(String clokeyId, int expectedSize, String month) {
        // given
        Long myMemberId = 1L;

        // when
        HistoryResponseDTO.MonthViewResult result = historyService.getMonthlyHistories(myMemberId, clokeyId, month);

        // then
        assertThat(result.getHistories().size()).isEqualTo(expectedSize);
    }

    @DisplayName("기록의 내용을 정확하게 반환한다.")
    @Test
    void 월별_기록_조회_성공_2() {
        // given
        Long myMemberId = 1L;
        String target = null;
        String month = "2025-01";

        // when
        HistoryResponseDTO.MonthViewResult monthViewResult = historyService.getMonthlyHistories(myMemberId, target, month);
        HistoryResponseDTO.HistoryResult result1 = monthViewResult.getHistories().get(0);
        HistoryResponseDTO.HistoryResult result2 = monthViewResult.getHistories().get(1);

        // then
        assertThat(result1)
                .extracting("historyId", "date", "imageUrl")
                .containsExactly(1L, LocalDate.of(2025, 1, 1), "https://example.com/images/new_year.jpg");

        assertThat(result2)
                .extracting("historyId", "date", "imageUrl")
                .containsExactly(2L, LocalDate.of(2025, 1, 2), "https://example.com/images/reading.jpg");
    }


    @DisplayName("비공개 기록의 url은 주인이 볼경우 보이고 아닌 경우 '비공개입니다'로 표시됩니다.")
    @ParameterizedTest(name = "myMemberId={0}, targetClokeyId={1}, 조회 월={2}, 결과 url={3}")
    @CsvSource(
            nullValues = "null",
            value = {
                    "1, clokey3, 2025-01, 비공개입니다", //1이 3번 Member의 비공개 history 조회
                    "3, null, 2025-01, https://example.com/imagefor6.jpg" //3번 Member가 자신의 비공개 history 조회
            }
    )
    void 월별_기록_조회_성공_3(Long myMemberId, String targetClokeyId, String month, String expectedUrl) {
        // given
        HistoryResponseDTO.MonthViewResult result = historyService.getMonthlyHistories(myMemberId, targetClokeyId, month);

        // when
        String historyImageUrl = result.getHistories()
                .get(0)
                .getImageUrl();

        // then
        assertThat(historyImageUrl).isEqualTo(expectedUrl);
    }

    @DisplayName("비공개 계정에서 본인은 본인의 기록을 열람할 수 있어야 한다.")
    @Test
    void 월별_기록_조회_성공_4() {
        // given
        Long myMemberId = 2L;
        String targetClokeyId = null;
        String month = "2025-01";

        //then
        assertThatCode(() ->
                historyService.getMonthlyHistories(myMemberId, targetClokeyId, month)
        ).doesNotThrowAnyException();
    }

    @DisplayName("존재하지 않는 clokeyId를 입력하면 Service단에서 예외가 발생합니다.")
    @ParameterizedTest
    @ValueSource(strings = {"없는ClokeyId", "예외", "", "  "})
    void 월별_기록_조회_예외_1(String targetClokeyId) {
        // given
        Long myId = 1L;
        String month = "2025-01";

        // then
        assertThatThrownBy(() -> historyService.getMonthlyHistories(myId, targetClokeyId, month))
                .isInstanceOfSatisfying(DatabaseException.class, ex ->
                        assertThat(ex.getCode()).isEqualTo(ErrorStatus.NO_SUCH_MEMBER)
                );
    }

    @DisplayName("존재하지 않는 clokeyId를 입력하면 Controller단에서 예외가 발생합니다.")
    @ParameterizedTest
    @ValueSource(strings = {"없는ClokeyId", "예외", "", "  "})
    void 월별_기록_조회_예외_2(String targetClokeyId) {
        // given
        Member currentMember = memberRepository.findById(1L).get();
        String month = "2025-01";

        // then
        assertThatThrownBy(() -> historyRestController.getMonthlyHistories(currentMember, targetClokeyId, month))
                .isInstanceOfSatisfying(ConstraintViolationException.class, ex ->
                        assertThat(ex.getMessage()).contains(ErrorStatus.NO_SUCH_MEMBER.name())
                );
    }

    @DisplayName("날짜 형식이 'YYYY-MM'에 맞지 않으면 Controller단에서 예외가 발생합니다.")
    @ParameterizedTest
    @ValueSource(strings = {"2025-1", "2025년 1월", "2025/1", "2025-11-01"})
    void 월별_기록_조회_예외_3(String month) {
        // given
        Member currentMember = memberRepository.findById(1L).get();
        String targetMember = null; // 자기 자신의 기록을 본다.

        // then
        assertThatThrownBy(() -> historyRestController.getMonthlyHistories(currentMember, targetMember, month))
                .isInstanceOfSatisfying(ConstraintViolationException.class, ex ->
                        assertThat(ex.getMessage()).contains(ErrorStatus.DATE_INVALID.name())
                );
    }

    @DisplayName("비공개 멤버를 타인이 조회하려고 시도하는 경우 service단에서 에러가 발생합니다.")
    @Test
    void 월별_기록_조회_예외_4() {
        // given
        Long myMemberId = 1L;
        String targetMemberClokeyId = "clokey2";
        String month = "2025-01";

        // then
        assertThatThrownBy(() -> historyService.getMonthlyHistories(myMemberId, targetMemberClokeyId, month))
                .isInstanceOfSatisfying(HistoryException.class, ex ->
                        assertThat(ex.getCode()).isEqualTo(ErrorStatus.NO_PERMISSION_TO_ACCESS_HISTORY)
                );
    }

    /*일별 기록 조회 TEST*/

    @DisplayName("비공개 유저가 자신의 비공개 기록을 열람하는 케이스, 모든 정보가 정확하게 보인다. (기본 기능 + 열람 권한 테스트)")
    @Test
    void 일별_기록_조회_성공_1(){
        // given
        Long memberId = 2L; // 비공개인 2번 멤버
        Long historyId = 3L; // 2번 멤버의 비공개 기록

        // when
        HistoryResponseDTO.DailyHistoryResult result = historyService.getDaily(historyId,memberId);

        // then
        // 단일 필드값 검증
        assertThat(result)
                .extracting("memberId","historyId","memberImageUrl","nickName","clokeyId","contents","visibility","likeCount","date","commentCount","isLiked")
                .containsExactly(2L,3L,"https://example.com/user2.png","User2","clokey2","새해를 맞아 여행을 다녀왔습니다.", false,1,LocalDate.of(2025,1,1),4L,true);

        // 컬랙션 필드 순차적 검증
        assertThat(result.getImageUrl().size()).isEqualTo(1);
        assertThat(result.getImageUrl().get(0)).isEqualTo("https://example.com/images/travel.jpg");

        assertThat(result.getHashtags().size()).isEqualTo(0);

        assertThat(result.getCloths().size()).isEqualTo(1);
        assertThat(result.getCloths().get(0))
                .extracting("clothId","clothImageUrl","clothName")
                .containsExactly(3L,"https://example.com/images/cloth3_1.jpg","검은색 셔츠");
    }

    @DisplayName("공개 유저인 타인의 공개 기록은 볼 수 있지만, 비공개 옷은 보이지 않는다.")
    @Test
    void 일별_기록_조회_성공_2() {
        // given
        Long memberId = 1L;
        Long historyId = 7L; // 공개 유저인 4번 멤버의 공개인 7번 기록.

        // when
        HistoryResponseDTO.DailyHistoryResult result = historyService.getDaily(historyId,memberId);

        // then
        assertThat(result.getCloths().size()).isEqualTo(0);
    }

    @DisplayName("존재하지 않는 historyId를 입력하면 Controller단에서 에러가 발생합니다.")
    @ParameterizedTest
    @ValueSource(longs = {100L, 2000L, 1234L})
    void 일별_기록_조회_예외_1(Long historyId) {
        // given
        Member member = memberRepository.findById(1L).get();

        // then
        assertThatThrownBy(() -> historyRestController.getDailyHistory(historyId,member))
                .isInstanceOfSatisfying(ConstraintViolationException.class, ex ->
                        assertThat(ex.getMessage()).contains(ErrorStatus.NO_SUCH_HISTORY.name())
                );
    }

    @DisplayName("비공개인 유저의 게시물 또는 공개인 유저의 비공개 게시물을 조회할 경우 서비스단에서 에러가 발생합니다.")
    @ParameterizedTest(name = "memberId={0}, targetHistoryID={1}")
    @CsvSource(
            nullValues = "null",
            value = {
                    "1, 4",     // 1번 Member가 2번 Member(비공개)가 작성한 공개 기록을 조회하려는 경우
                    "1, 8" // 1번 Member가 공개 Member인 4번의 비공개 기록 8번을 조회하려는 경우
            }
    )
    void 일별_기록_조회_예외_2(Long memberId, Long historyId) {

        // then
        assertThatThrownBy(() -> historyService.getDaily(historyId,memberId))
                .isInstanceOfSatisfying(HistoryException.class, ex ->
                        assertThat(ex.getCode()).isEqualTo(ErrorStatus.NO_PERMISSION_TO_ACCESS_HISTORY)
                );
    }

    /* 나의 기록인지 확인하기 API TEST*/

    @DisplayName("나와 타인의 기록을 정확하게 판정한다.")
    @ParameterizedTest(name = "memberId={0}, historyId={1}, expected={2}")
    @CsvSource(
            nullValues = "null",
            value = {
                    "1, 1, true",
                    "1, 2, true",
                    "1, 3, false",
                    "1, 5, false"
            }
    )
    void 나의_기록인지_확인하기_성공_1(Long memberId, Long historyId, boolean expected) {

        //when
        boolean result = historyService.checkIfHistoryIsMine(historyId,memberId).getIsMyHistory();

        // then
        assertThat(result).isEqualTo(expected);
    }

    @DisplayName("존재하지 않는 historyId를 입력할 경우 Controller단에서 에러가 발생한다.")
    @ParameterizedTest
    @ValueSource(longs = {100L,2000L,1234L})
    void 나의_기록인지_확인하기_에외_1(Long historyId){

        // given
        Member member = memberRepository.findById(1L).get();

        // then
        assertThatThrownBy(() -> historyRestController.checkIfHistoryIsMine(historyId,member))
                .isInstanceOfSatisfying(ConstraintViolationException.class, ex ->
                        assertThat(ex.getMessage()).contains(ErrorStatus.NO_SUCH_HISTORY.name())
                );
    }

    /* 좋아요 누른 사람들 정보 확인하기 API TEST*/

    @DisplayName("좋아요 누른 사람들 정보를 정확하게 반환합니다.")
    @Test
    void 좋아요_누른_사람들_확인하기_성공_1() {

        // given
        Long checkingMemberId = 1L;
        Long historyId = 2L;

        // when
        List<HistoryResponseDTO.LikedUserResult> result = historyService.getLikedUsers(checkingMemberId, historyId).getLikedUsers();

        //then
        // 결과와 내용을 순차적으로 확인
        assertThat(result.size()).isEqualTo(3);

        HistoryResponseDTO.LikedUserResult result1 = result.get(0);
        HistoryResponseDTO.LikedUserResult result2 = result.get(1);
        HistoryResponseDTO.LikedUserResult result3 = result.get(2);

        assertThat(result1)
                .extracting("memberId", "clokeyId", "ImageUrl","nickname","followStatus","isMe")
                .containsExactly(1L,"clokey1","https://example.com/user1.png","User1",false,true);

        assertThat(result2)
                .extracting("memberId", "clokeyId", "ImageUrl","nickname","followStatus","isMe")
                .containsExactly(2L,"clokey2","https://example.com/user2.png","User2",true,false);

        assertThat(result3)
                .extracting("memberId", "clokeyId", "ImageUrl","nickname","followStatus","isMe")
                .containsExactly(4L,"clokey4","https://example.com/user4.png","User4",true,false);
    }

    @DisplayName("존재하지 않는 historyId를 입력할 경우 Controller단에서 에러가 발생한다.")
    @ParameterizedTest
    @ValueSource(longs = {100L,2000L,1234L})
    void 좋아요_누른_사람들_확인하기_예외_1(Long historyId){

        // given
        Member member = memberRepository.findById(1L).get();

        // then
        assertThatThrownBy(() -> historyRestController.getLikedUsers(historyId,member))
                .isInstanceOfSatisfying(ConstraintViolationException.class, ex ->
                        assertThat(ex.getMessage()).contains(ErrorStatus.NO_SUCH_HISTORY.name())
                );
    }

    @DisplayName("비공개인 유저의 게시물 또는 공개인 유저의 비공개 게시물의 좋아요 목록을 조회할 경우 서비스단에서 에러가 발생합니다.")
    @ParameterizedTest(name = "memberId={0}, targetHistoryID={1}")
    @CsvSource(
            nullValues = "null",
            value = {
                    "1, 4",     // 1번 Member가 2번 Member(비공개)가 작성한 공개 기록을 조회하려는 경우
                    "1, 8" // 1번 Member가 공개 Member인 4번의 비공개 기록 8번을 조회하려는 경우
            }
    )
    void 좋아요_누른_사람들_확인하기_예외_2(Long memberId, Long historyId) {

        // then
        assertThatThrownBy(() -> historyService.getLikedUsers(memberId,historyId))
                .isInstanceOfSatisfying(HistoryException.class, ex ->
                        assertThat(ex.getCode()).isEqualTo(ErrorStatus.NO_PERMISSION_TO_ACCESS_HISTORY)
                );
    }

    /* 댓글 조회 API */

    @DisplayName("정확한 댓글 조회 기능을 수행한다.")
    @Test
    void 댓글_조회_성공_1() {

        // given
        Long historyId = 1L;
        int page = 0;

        // when
        HistoryResponseDTO.HistoryCommentResult result = historyService.getComments(historyId,page);

        // then
        // 결과 내용을 순차적으로 확인
        // HistoryResponseDTO.HistoryCommentResult 내용 확인
        assertThat(result)
                .extracting("totalPage","totalElements","isFirst","isLast")
                .containsExactly(1,4,true,true);
        assertThat(result.getComments().size()).isEqualTo(2);

        // HistoryResponseDTO.CommentResult (각각 댓글의 내용 확인)
        HistoryResponseDTO.CommentResult commentResult1 = result.getComments().get(0);
        HistoryResponseDTO.CommentResult commentResult2 = result.getComments().get(1);

        assertThat(commentResult1)
                .extracting("commentId","clokeyId","nickName","userImageUrl","content")
                .containsExactly(1L,"clokey1","User1","https://example.com/user1.png","첫 번째 댓글");
        assertThat(commentResult1.getReplyResults().size()).isEqualTo(2);

        assertThat(commentResult2)
                .extracting("commentId","clokeyId","nickName","userImageUrl","content")
                .containsExactly(11L,"clokey1","User1","https://example.com/user1.png","첫 번째 댓글");
        assertThat(commentResult2.getReplyResults().size()).isEqualTo(0);

        // HistoryResponseDTO.ReplyResult (각각 댓글의 대댓글 확인)
        HistoryResponseDTO.ReplyResult replyResult1 = commentResult1.getReplyResults().get(0);
        HistoryResponseDTO.ReplyResult replyResult2 = commentResult1.getReplyResults().get(1);

        assertThat(replyResult1)
                .extracting("commentId","clokeyId","nickName","userImageUrl","content")
                .containsExactly(6L,"clokey2","User2","https://example.com/user2.png","첫 번째 댓글에 대한 대댓글");

        assertThat(replyResult2)
                .extracting("commentId","clokeyId","nickName","userImageUrl","content")
                .containsExactly(16L,"clokey2","User2","https://example.com/user2.png","첫 번째 댓글에 대한 대댓글");

    }

    @DisplayName("존재하지 않는 historyId를 입력할 경우 Controller단에서 에러가 발생한다.")
    @ParameterizedTest
    @ValueSource(longs = {100L,2000L,1234L})
    void 댓글_조회_예외_1(Long historyId){

        // given
        int page = 1;

        // then
        assertThatThrownBy(() -> historyRestController.getComments(historyId,page))
                .isInstanceOfSatisfying(ConstraintViolationException.class, ex ->
                        assertThat(ex.getMessage()).contains(ErrorStatus.NO_SUCH_HISTORY.name())
                );
    }

    @DisplayName("0보다 작은 페이지 값을 입력할 경우 Controller단에서 에러가 발생한다.")
    @ParameterizedTest
    @ValueSource(ints = {-1000, -10, 0})
    void 댓글_조회_예외_2(int page){

        // given
        Long historyId = 1L;

        // then
        assertThatThrownBy(() -> historyRestController.getComments(historyId,page))
                .isInstanceOfSatisfying(ConstraintViolationException.class, ex ->
                        assertThat(ex.getMessage()).contains(ErrorStatus.PAGE_UNDER_ONE.name())
                );
    }

}
