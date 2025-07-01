package com.clokey.server.domain.history.application;

import com.clokey.server.domain.cloth.domain.repository.ClothRepository;
import com.clokey.server.domain.cloth.exception.ClothException;
import com.clokey.server.domain.history.api.HistoryRestController;
import com.clokey.server.domain.history.domain.entity.Comment;
import com.clokey.server.domain.history.domain.repository.*;
import com.clokey.server.domain.history.dto.HistoryRequestDTO;
import com.clokey.server.domain.history.dto.HistoryResponseDTO;
import com.clokey.server.domain.history.exception.HistoryException;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.member.domain.repository.MemberRepository;
import com.clokey.server.domain.model.entity.enums.Visibility;
import com.clokey.server.global.error.code.status.ErrorStatus;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Disabled
@SpringBootTest
@Transactional
@ActiveProfiles("local")
public class HistoryCreateTest {

    @Autowired
    private HistoryService historyService;

    @Autowired
    private HistoryRestController historyRestController;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ClothRepository clothRepository;

    @Autowired
    private HistoryClothRepository historyClothRepository;

    @Autowired
    private HashtagRepository hashtagRepository;

    @Autowired
    private HashtagHistoryRepository hashtagHistoryRepository;

    @Autowired
    private MemberLikeRepository memberLikeRepository;

    @Autowired
    private CommentRepository commentRepository;

    private List<MultipartFile> imageFiles;

    @BeforeEach
    void setUp() {

        MockMultipartFile imageFile1 = new MockMultipartFile(
                "imageFile",
                "sample.jpg",
                "image/jpeg",
                "test-image-content".getBytes()
        );
        imageFiles = List.of(imageFile1);
    }

    /**
     * 날짜별 옷 기록 추가 API TEST
     */

    @DisplayName("옷의 착용 횟수와 매핑 테이블에 새로운 필드가 반영된다.")
    @Test
    void 기록_생성_성공_옷등록() {

        /**
         *  최초 생성시 옷에 대한 정보를 등록한다.
         */
        // given
        Long memberId = 1L;

        HistoryRequestDTO.HistoryCreate requestDto = HistoryRequestDTO.HistoryCreate.builder()
                .content("테스트 내용")
                .clothes(List.of(1L, 2L))
                .hashtags(List.of("새해결심"))
                .visibility(Visibility.PUBLIC)
                .date("2025-02-02")
                .build();

        // when
        HistoryResponseDTO.HistoryCreateResult result = historyService.createHistory(requestDto, memberId, imageFiles);

        // then
        // 성공적으로 다음 history 생성
        assertThat(result.getHistoryId()).isNotNull();

        // 옷의 착용 횟수 증가 확인
        assertThat(clothRepository.findById(1L).get().getWearNum()).isEqualTo(7);
        assertThat(clothRepository.findById(2L).get().getWearNum()).isEqualTo(17);

        // 기록- 옷 매핑 테이블 추가 확인
        assertThat(historyClothRepository.existsByHistoryIdAndClothId(result.getHistoryId(), 1L)).isTrue();
        assertThat(historyClothRepository.existsByHistoryIdAndClothId(result.getHistoryId(), 2L)).isTrue();

        /**
         *  같은 날짜에 대해서 기록을 다시 만들 경우 옷의 변화를 반영한다 ( 없어진 옷을 내리고 새로 생긴 옷 등록 )
         */

        // given
        HistoryRequestDTO.HistoryCreate updateRequest = HistoryRequestDTO.HistoryCreate.builder()
                .content("테스트 내용")
                .clothes(List.of(1L, 11L))
                .hashtags(List.of("새해결심"))
                .visibility(Visibility.PUBLIC)
                .date("2025-02-02")
                .build();

        // when
        HistoryResponseDTO.HistoryCreateResult updateResult = historyService.createHistory(updateRequest, memberId, imageFiles);

        // then
        // 옷 착용 횟수 업데이트 확인 ( 1L : 유지 , 2L : 1감소 , 11L : 1증가 )
        assertThat(clothRepository.findById(1L).get().getWearNum()).isEqualTo(7);
        assertThat(clothRepository.findById(2L).get().getWearNum()).isEqualTo(16);
        assertThat(clothRepository.findById(11L).get().getWearNum()).isEqualTo(1);

        // 기록- 옷 매핑 테이블 업데이트 확인 ( 1L : 유지 , 2L : 삭제 , 11L : 추가 )
        assertThat(historyClothRepository.existsByHistoryIdAndClothId(result.getHistoryId(), 1L)).isTrue();
        assertThat(historyClothRepository.existsByHistoryIdAndClothId(result.getHistoryId(), 2L)).isFalse();
        assertThat(historyClothRepository.existsByHistoryIdAndClothId(result.getHistoryId(), 11L)).isTrue();
    }

    @DisplayName("기존 해시태그가 매핑 테이블에 추가되거나 새로운 해시태그가 추가된다, 한 번 생성된 해시태그는 삭제되지는 않는다.")
    @Test
    void 기록_생성_성공_해시태그_등록() {

        /**
         *  존재하지 않는 해시태그를 기록에 등록할 경우 해시태그가 생성되고 매핑 테이블에 추가된다.
         */
        // given
        Long memberId = 1L;

        HistoryRequestDTO.HistoryCreate requestDto = HistoryRequestDTO.HistoryCreate.builder()
                .content("테스트 내용")
                .clothes(List.of(1L, 2L))
                .hashtags(List.of("새로운 테스트 해시태그"))
                .visibility(Visibility.PUBLIC)
                .date("2025-02-02")
                .build();

        assertThat(hashtagRepository.findByName("새로운 테스트 해시태그").isPresent()).isFalse();

        // when
        HistoryResponseDTO.HistoryCreateResult result = historyService.createHistory(requestDto, memberId, imageFiles);

        // then
        // 해시태그가 생성됨
        assertThat(hashtagRepository.findByName("새로운 테스트 해시태그").isPresent()).isTrue();

        // 매핑 테이블에 추가됨
        assertThat(hashtagHistoryRepository.existsByHashtagIdAndHistoryId(
                hashtagRepository.findByName("새로운 테스트 해시태그").get().getId(),
                result.getHistoryId()
        )).isTrue();

        /**
         *  같은 날짜에 대해서 해시태그를 업데이트 하면 해시태그의 변화를 반영한다 ( 기록에서 제외될 수 있으나 해시태그는 삭제되지 않음 )
         */

        // given
        HistoryRequestDTO.HistoryCreate updateRequestDto = HistoryRequestDTO.HistoryCreate.builder()
                .content("테스트 내용")
                .clothes(List.of(1L, 2L))
                .hashtags(List.of("새해결심"))
                .visibility(Visibility.PUBLIC)
                .date("2025-02-02")
                .build();

        // when
        HistoryResponseDTO.HistoryCreateResult updateResult = historyService.createHistory(updateRequestDto, memberId, imageFiles);

        // then
        // 새로운 해시태그 "시해 결심"은 매핑 테이블에 추가되고 "새로운 테스트 해시태그"는 제외된다, 하지만 삭제되지는 않는다.
        assertThat(hashtagHistoryRepository.existsByHashtagIdAndHistoryId(
                hashtagRepository.findByName("새해결심").get().getId(),
                updateResult.getHistoryId()
        )).isTrue();
        assertThat(hashtagRepository.findByName("새로운 테스트 해시태그").isPresent()).isTrue();
        assertThat(hashtagHistoryRepository.existsByHashtagIdAndHistoryId(
                hashtagRepository.findByName("새로운 테스트 해시태그").get().getId(),
                updateResult.getHistoryId()
        )).isFalse();
    }

    @DisplayName("신고당한 기록을 PUBLIC으로 전화하려 시도하는 경우 Service단에서 에러를 던집니다.")
    @Test
    void 기록_생성_예외1() {
        // given
        Long memberId = 1L;

        HistoryRequestDTO.HistoryCreate requestDto = HistoryRequestDTO.HistoryCreate.builder()
                .content("테스트 내용")
                .clothes(List.of(1L, 2L))
                .hashtags(List.of("새해결심"))
                .visibility(Visibility.PUBLIC)
                .date("2025-11-01")
                .build();

        // then
        assertThatThrownBy(() -> historyService.createHistory(requestDto, memberId, imageFiles))
                .isInstanceOfSatisfying(HistoryException.class, ex ->
                        assertThat(ex.getCode()).isEqualTo(ErrorStatus.BANNED_HISTORY_TO_PUBLIC)
                );
    }

    @DisplayName("나의 옷이 아닌 옷을 등록하려 하는 경우 Service에서 에러를 던집니다")
    @Test
    void 기록_생성_예외2() {
        // given
        Long memberId = 1L;

        HistoryRequestDTO.HistoryCreate requestDto = HistoryRequestDTO.HistoryCreate.builder()
                .content("테스트 내용")
                .clothes(List.of(3L))
                .hashtags(List.of("새해결심"))
                .visibility(Visibility.PUBLIC)
                .date("2025-02-02")
                .build();

        // then
        assertThatThrownBy(() -> historyService.createHistory(requestDto, memberId, imageFiles))
                .isInstanceOfSatisfying(ClothException.class, ex ->
                        assertThat(ex.getCode()).isEqualTo(ErrorStatus.NOT_MY_CLOTH)
                );
    }

    @DisplayName("날짜 형식이 'YYYY-MM-DD'에 맞지 않으면 Controller단에서 예외가 발생합니다.")
    @ParameterizedTest
    @ValueSource(strings = {"2025-1-1", "2025년 1월 1일", "2025/1/1", "2025-11"})
    void 기록_생성_예외3(String month) {
        // given
        Member member = memberRepository.findById(1L).get();

        HistoryRequestDTO.HistoryCreate requestDto = HistoryRequestDTO.HistoryCreate.builder()
                .content("테스트 내용")
                .clothes(List.of(1L, 2L))
                .hashtags(List.of("새해결심"))
                .visibility(Visibility.PUBLIC)
                .date(month)
                .build();

        // then
        assertThatThrownBy(() -> historyRestController.createHistory(requestDto, imageFiles, member))
                .isInstanceOfSatisfying(ConstraintViolationException.class, ex ->
                        assertThat(ex.getMessage()).contains(ErrorStatus.DATE_INVALID.name())
                );
    }

    @DisplayName("content가 200자가 넘어가는 경우 Controller에서 예외가 발생합니다.")
    @Test
    void 기록_생성_예외4() {
        // given
        Member member = memberRepository.findById(1L).get();

        HistoryRequestDTO.HistoryCreate requestDto = HistoryRequestDTO.HistoryCreate.builder()
                .content("a".repeat(201))
                .clothes(List.of(1L, 2L))
                .hashtags(List.of("새해결심"))
                .visibility(Visibility.PUBLIC)
                .date("2025-02-02")
                .build();

        // then
        assertThatThrownBy(() -> historyRestController.createHistory(requestDto, imageFiles, member))
                .isInstanceOfSatisfying(ConstraintViolationException.class, ex ->
                        assertThat(ex.getMessage()).contains(ErrorStatus.HISTORY_CONTENT_OUT_OF_RANGE.name())
                );
    }

    @DisplayName("중복된 cloth를 등록하려는 경우 Controller에서 예외가 발생합니다.")
    @Test
    void 기록_생성_예외5() {
        // given
        Member member = memberRepository.findById(1L).get();

        HistoryRequestDTO.HistoryCreate requestDto = HistoryRequestDTO.HistoryCreate.builder()
                .content("테스트 내용")
                .clothes(List.of(1L, 1L))
                .hashtags(List.of("새해결심"))
                .visibility(Visibility.PUBLIC)
                .date("2025-02-02")
                .build();

        // then
        assertThatThrownBy(() -> historyRestController.createHistory(requestDto, imageFiles, member))
                .isInstanceOfSatisfying(ConstraintViolationException.class, ex ->
                        assertThat(ex.getMessage()).contains(ErrorStatus.DUPLICATE_CLOTHES_FOR_HISTORY.name())
                );
    }

    @DisplayName("11개 이상의 사진을 기록에 등록하려는 경우 Controller단에서 에러를 던집니다.")
    @Test
    void 기록_생성_예외6() {
        // given
        Member member = memberRepository.findById(1L).get();

        HistoryRequestDTO.HistoryCreate requestDto = HistoryRequestDTO.HistoryCreate.builder()
                .content("테스트 내용")
                .clothes(List.of(1L, 1L))
                .hashtags(List.of("새해결심"))
                .visibility(Visibility.PUBLIC)
                .date("2025-02-02")
                .build();

        List<MultipartFile> filesOf11Images = new ArrayList<>();

        for (int i = 0; i < 11; i++) {
            MultipartFile file = new MockMultipartFile(
                    "imageFile",                        // name
                    "test-image-" + i + ".jpg",         // original filename
                    "image/jpeg",                       // content type
                    "dummy-image-content".getBytes()    // file content
            );
            filesOf11Images.add(file);
        }
        // then
        assertThatThrownBy(() -> historyRestController.createHistory(requestDto, filesOf11Images, member))
                .isInstanceOfSatisfying(ConstraintViolationException.class, ex ->
                        assertThat(ex.getMessage()).contains(ErrorStatus.IMAGE_QUANTITY_OVER_HISTORY_IMAGE_LIMIT.name())
                );
    }

    @DisplayName("기록에 사진을 등록하지 않는 경우 Controller단에서 에러를 던집니다.")
    @Test
    void 기록_생성_예외7() {
        // given
        Member member = memberRepository.findById(1L).get();

        HistoryRequestDTO.HistoryCreate requestDto = HistoryRequestDTO.HistoryCreate.builder()
                .content("테스트 내용")
                .clothes(List.of(1L, 2L))
                .hashtags(List.of("새해결심"))
                .visibility(Visibility.PUBLIC)
                .date("2025-02-02")
                .build();

        List<MultipartFile> emptyImageFile = new ArrayList<>();


        // then
        assertThatThrownBy(() -> historyRestController.createHistory(requestDto, emptyImageFile, member))
                .isInstanceOfSatisfying(ConstraintViolationException.class, ex ->
                        assertThat(ex.getMessage()).contains(ErrorStatus.IMAGE_QUANTITY_OVER_HISTORY_IMAGE_LIMIT.name())
                );
    }

    @DisplayName("옷을 등록하지 않는 경우 Controller단에서 에러를 던집니다.")
    @Test
    void 기록_생성_예외8() {
        // given
        Member member = memberRepository.findById(1L).get();

        HistoryRequestDTO.HistoryCreate requestDto = HistoryRequestDTO.HistoryCreate.builder()
                .content("테스트 내용")
                .clothes(List.of())
                .hashtags(List.of("새해결심"))
                .visibility(Visibility.PUBLIC)
                .date("2025-02-02")
                .build();

        // then
        assertThatThrownBy(() -> historyRestController.createHistory(requestDto, imageFiles, member))
                .isInstanceOfSatisfying(ConstraintViolationException.class, ex ->
                        assertThat(ex.getMessage()).contains(ErrorStatus.NO_CLOTH_FOR_HISTORY.name())
                );
    }

    /**
     * 좋아요 API TEST
     */

    @DisplayName("좋아요 취소 기능")
    @Test
    void 좋아요_성공1() {
        // given
        // member 1은 history 10번을 좋아요 한 상태이다.
        Long memberId = 1L;
        Long historyId = 10L;
        boolean isLiked = true;
        assertThat(memberLikeRepository.existsByMemberIdAndHistoryId(1L, 10L)).isTrue();

        // when
        // 좋아요 취소
        HistoryResponseDTO.LikeResult result = historyService.changeLike(memberId, historyId, isLiked);

        // then
        assertThat(memberLikeRepository.existsByMemberIdAndHistoryId(1L, 10L)).isFalse();
        assertThat(result)
                .extracting("historyId", "isLiked", "likeCount")
                .containsExactly(10L, false, 0);
    }

    @DisplayName("좋아요 하기 기능")
    @Test
    void 좋아요_성공2() {
        // given
        // member 1은 history 3번을 좋아요 하지 않은 상태다
        Long memberId = 1L;
        Long historyId = 3L;
        boolean isLiked = false;
        assertThat(memberLikeRepository.existsByMemberIdAndHistoryId(1L, 3L)).isFalse();

        // when
        // 좋아요 누르기
        HistoryResponseDTO.LikeResult result = historyService.changeLike(memberId, historyId, isLiked);

        // then
        assertThat(memberLikeRepository.existsByMemberIdAndHistoryId(1L, 3L)).isTrue();
        assertThat(result)
                .extracting("historyId", "isLiked", "likeCount")
                .containsExactly(3L, true, 2);
    }

    @DisplayName("존재하지 않는 historyId를 입력할 경우 Controller단에서 에러가 발생한다.")
    @ParameterizedTest
    @ValueSource(longs = {100L, 2000L, 1234L})
    void 좋아요_예외1(Long historyId) {

        // given
        Member member = memberRepository.findById(1L).get();
        HistoryRequestDTO.LikeStatusChange requestDto = HistoryRequestDTO.LikeStatusChange.builder()
                .historyId(historyId)
                .isLiked(false)
                .build();

        // then
        assertThatThrownBy(() -> historyRestController.like(member, requestDto))
                .isInstanceOfSatisfying(ConstraintViolationException.class, ex ->
                        assertThat(ex.getMessage()).contains(ErrorStatus.NO_SUCH_HISTORY.name())
                );
    }

    @DisplayName("좋아요 정보가 잘못된 경우 Serivce단에서 에러를 던집니다")
    @ParameterizedTest(name = "historyId={0}, 좋아요 누른 멤버={1}, 좋아요 여부={2}")
    @CsvSource(
            nullValues = "null",
            value = {
                    "1,1,false", //거짓 - 1번 멤버는 1번 history에 좋아요한 상태
                    "3,1, true"  //거짓 - 1번 멤버는 3번 history에 좋아요를 누르지 않음
            }
    )
    void 좋아요_예외2(Long historyId, Long memberId, boolean isLiked) {

        //then
        assertThatThrownBy(() -> historyService.changeLike(memberId, historyId, isLiked))
                .isInstanceOfSatisfying(HistoryException.class, ex ->
                        assertThat(ex.getCode()).isEqualTo(ErrorStatus.IS_LIKED_INVALID)
                );
    }

    /**
     * 댓글 작성 API TEST
     */

    @DisplayName("부모 댓글을 정확하게 작성할 수 있다.")
    @Test
    void 댓글_작성_성공1() {

        // given
        Long historyToWriteComment = 1L;
        Long parentCommentId = null;
        Long commentWriter = 1L;
        String content = "테스트 부모 댓글";

        // when
        HistoryResponseDTO.CommentWriteResult result = historyService.writeComment(historyToWriteComment, parentCommentId, commentWriter, content);
        Comment writtenComment = commentRepository.findById(result.getCommentId()).get();

        // then
        assertThat(writtenComment.getHistory().getId()).isEqualTo(historyToWriteComment);
        assertThat(writtenComment.getComment()).isNull();
        assertThat(writtenComment.getMember().getId()).isEqualTo(commentWriter);
        assertThat(writtenComment.getContent()).isEqualTo(content);
    }

    @DisplayName("대댓글을 정확하게 작성할 수 있다.")
    @Test
    void 댓글_작성_성공2() {

        // given
        Long historyToWriteComment = 1L;
        Long parentCommentId = 1L;
        Long commentWriter = 1L;
        String content = "테스트 대댓글";

        // when
        HistoryResponseDTO.CommentWriteResult result = historyService.writeComment(historyToWriteComment, parentCommentId, commentWriter, content);
        Comment writtenComment = commentRepository.findById(result.getCommentId()).get();

        // then
        assertThat(writtenComment.getHistory().getId()).isEqualTo(historyToWriteComment);
        assertThat(writtenComment.getComment().getId()).isEqualTo(parentCommentId);
        assertThat(writtenComment.getMember().getId()).isEqualTo(commentWriter);
        assertThat(writtenComment.getContent()).isEqualTo(content);
    }

    @DisplayName("깊이 2이상의 댓글 (대댓글에 대댓글 달기)을 작성하면 Controller단에서 에러를 던진다.")
    @Test
    void 댓글_작성_예외1() {

        // given
        Long historyToWriteComment = 1L;
        Member commentWriter = memberRepository.findById(1L).get();
        HistoryRequestDTO.CommentWrite requestDto = HistoryRequestDTO.CommentWrite.builder()
                .commentId(6L) //이미 1번 history의 1번 댓글에 달린 대댓글
                .content("테스트 깊이 2댓글")
                .build();

        // then
        assertThatThrownBy(() -> historyRestController.writeComments(historyToWriteComment, commentWriter, requestDto))
                .isInstanceOfSatisfying(ConstraintViolationException.class, ex ->
                        assertThat(ex.getMessage()).contains(ErrorStatus.NESTED_COMMENT.name())
                );

    }

    @DisplayName("존재하지 않는 historyId를 입력할 경우 Controller단에서 에러가 발생한다.")
    @ParameterizedTest
    @ValueSource(longs = {100L, 2000L, 1234L})
    void 댓글_작성_예외2(Long historyId) {

        // given
        Member commentWriter = memberRepository.findById(1L).get();
        HistoryRequestDTO.CommentWrite requestDto = HistoryRequestDTO.CommentWrite.builder()
                .commentId(6L) //이미 1번 history의 1번 댓글에 달린 대댓글
                .content("테스트 댓글")
                .build();

        // then
        assertThatThrownBy(() -> historyRestController.writeComments(historyId, commentWriter, requestDto))
                .isInstanceOfSatisfying(ConstraintViolationException.class, ex ->
                        assertThat(ex.getMessage()).contains(ErrorStatus.NO_SUCH_HISTORY.name())
                );
    }

    @DisplayName("Content에 빈칸 또는 null값을 입력하는 경우 Controller단에서 예외를 던진다.")
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "  "})
    void 댓글_작성_예외3(String content) {

        // given
        Long historyToWriteComment = 1L;
        Member commentWriter = memberRepository.findById(1L).get();
        HistoryRequestDTO.CommentWrite requestDto = HistoryRequestDTO.CommentWrite.builder()
                .commentId(1L)
                .content(content)
                .build();

        // then
        assertThatThrownBy(() -> historyRestController.writeComments(historyToWriteComment, commentWriter, requestDto))
                .isInstanceOf(ValidationException.class);
    }

    @DisplayName("Content가 50자를 넘어가면 Controller단에서 에러를 던집니다.")
    @Test
    void 댓글_작성_예외4() {
        // given
        Long historyToWriteComment = 1L;
        Member commentWriter = memberRepository.findById(1L).get();
        HistoryRequestDTO.CommentWrite requestDto = HistoryRequestDTO.CommentWrite.builder()
                .commentId(1L)
                .content("a".repeat(51))
                .build();

        assertThatThrownBy(() -> historyRestController.writeComments(historyToWriteComment, commentWriter, requestDto))
                .isInstanceOfSatisfying(ConstraintViolationException.class, ex ->
                        assertThat(ex.getMessage()).contains(ErrorStatus.COMMENT_LENGTH_OUT_OF_RANGE.name())
                );
    }

    @DisplayName("존재하지 않는 historyId를 입력할 경우 Service단에서 에러를 던진다.")
    @ParameterizedTest
    @ValueSource(longs = {100L, 2000L, 1234L})
    void 댓글_작성_예외5(Long parentCommentId) {

        // given
        Long historyToWriteComment = 1L;
        Long commentWriter = 1L;
        String content = "테스트 댓글";

        // then
        assertThatThrownBy(() -> historyService.writeComment(historyToWriteComment, parentCommentId, commentWriter, content))
                .isInstanceOf(HistoryException.class);
    }

    @DisplayName("부모 댓글과 대댓글로 작성하는 댓글의 historyId가 다른 경우 Service단에서 에러를 던진다.")
    @Test
    void 댓글_작성_예외6() {

        //given
        Long historyToWriteComment = 2L; // 거짓 : 1번 댓글은 1번 history에 작성되었습니다.
        Long parentComment = 1L;
        Long commentWriter = 1L;
        String content = "테스트댓글";

        //then
        assertThatThrownBy(() -> historyService.writeComment(historyToWriteComment, parentComment, commentWriter, content))
                .isInstanceOfSatisfying(HistoryException.class, ex ->
                        assertThat(ex.getCode()).isEqualTo(ErrorStatus.PARENT_COMMENT_HISTORY_ERROR)
                );
    }
}

