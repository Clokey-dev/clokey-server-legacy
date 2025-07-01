package com.clokey.server.domain.history.domain.repository;

import com.clokey.server.domain.category.domain.entity.Category;
import com.clokey.server.domain.category.domain.repostiory.CategoryRepository;
import com.clokey.server.domain.cloth.domain.entity.Cloth;
import com.clokey.server.domain.cloth.domain.entity.ClothImage;
import com.clokey.server.domain.cloth.domain.repository.ClothImageRepository;
import com.clokey.server.domain.cloth.domain.repository.ClothRepository;
import com.clokey.server.domain.history.domain.entity.*;
import com.clokey.server.domain.history.dto.projection.DailyHistoryClothProjectionDTO;
import com.clokey.server.domain.history.dto.projection.HistoryCommentProjectionDTO;
import com.clokey.server.domain.history.dto.projection.HistoryProjectionDTO;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.member.domain.repository.MemberRepository;
import com.clokey.server.domain.model.entity.enums.MemberStatus;
import com.clokey.server.domain.model.entity.enums.SocialType;
import com.clokey.server.domain.model.entity.enums.ThicknessLevel;
import com.clokey.server.domain.model.entity.enums.Visibility;
import com.clokey.server.global.config.QuerydslConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(QuerydslConfig.class)
class HistoryProjectionRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private HistoryClothRepository historyClothRepository;

    @Autowired
    private MemberLikeRepository memberLikeRepository;

    @Autowired
    private ClothRepository clothRepository;

    @Autowired
    private ClothImageRepository clothImageRepository;

    @Autowired
    private HistoryImageRepository historyImageRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    @Qualifier("historyProjectionRepositoryImpl")
    private HistoryProjectionRepository historyProjectionRepository;

    @BeforeAll
    void setup() {

        Category topCategory = new Category(null, "상의", null);
        categoryRepository.save(topCategory);

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

        memberLikeRepository.saveAll(List.of(
                new MemberLike(null, member1, h1),
                new MemberLike(null, member1, h2),
                new MemberLike(null, member2, h3),
                new MemberLike(null, member3, h4),
                new MemberLike(null, member3, h5),
                new MemberLike(null, member3, h6),
                new MemberLike(null, member4, h7),
                new MemberLike(null, member4, h8),
                new MemberLike(null, member5, h9),
                new MemberLike(null, member2, h2),
                new MemberLike(null, member4, h2),
                new MemberLike(null, member1, h10)
        ));

        Comment c1 = commentRepository.save(new Comment(null, "첫 번째 댓글", member1, h1, null, false));
        Comment c2 = commentRepository.save(new Comment(null, "두 번째 댓글", member2, h2, null, false));
        Comment c3 = commentRepository.save(new Comment(null, "세 번째 댓글", member3, h3, null, false));
        Comment c4 = commentRepository.save(new Comment(null, "네 번째 댓글", member4, h4, null, false));
        Comment c5 = commentRepository.save(new Comment(null, "다섯 번째 댓글", member5, h5, null, false));

        commentRepository.saveAll(List.of(
                new Comment(null, "첫 번째 댓글에 대한 대댓글", member2, h1, c1, false),
                new Comment(null, "두 번째 댓글에 대한 대댓글", member3, h2, c2, false),
                new Comment(null, "세 번째 댓글에 대한 대댓글", member4, h3, c3, false),
                new Comment(null, "네 번째 댓글에 대한 대댓글", member5, h4, c4, false),
                new Comment(null, "다섯 번째 댓글에 대한 대댓글", member1, h5, c5, false)
        ));
        List<Cloth> cloths = new ArrayList<>();

        cloths.add(clothRepository.save(Cloth.builder()
                .name("흰색 맨투맨")
                .wearNum(5)
                .visibility(Visibility.PUBLIC)
                .tempUpperBound(20)
                .tempLowerBound(10)
                .thicknessLevel(ThicknessLevel.LEVEL_0)
                .category(topCategory)
                .member(member1)
                .clothUrl("https://example.com/images/cloth1.jpg")
                .brand("브랜드A")
                .build()));

        cloths.add(clothRepository.save(Cloth.builder()
                .name("청바지")
                .wearNum(15)
                .visibility(Visibility.PRIVATE)
                .tempUpperBound(15)
                .tempLowerBound(5)
                .thicknessLevel(ThicknessLevel.LEVEL_0)
                .category(topCategory)
                .member(member1)
                .clothUrl("https://example.com/images/cloth2.jpg")
                .brand("브랜드B")
                .build()));

        cloths.add(clothRepository.save(Cloth.builder()
                .name("검은색 셔츠")
                .wearNum(10)
                .visibility(Visibility.PUBLIC)
                .tempUpperBound(25)
                .tempLowerBound(15)
                .thicknessLevel(ThicknessLevel.LEVEL_0)
                .category(topCategory)
                .member(member2)
                .clothUrl("https://example.com/images/cloth3.jpg")
                .brand("브랜드C")
                .build()));

        cloths.add(clothRepository.save(Cloth.builder()
                .name("회색 코트")
                .wearNum(3)
                .visibility(Visibility.PUBLIC)
                .tempUpperBound(15)
                .tempLowerBound(0)
                .thicknessLevel(ThicknessLevel.LEVEL_0)
                .category(topCategory)
                .member(member2)
                .clothUrl("https://example.com/images/cloth4.jpg")
                .brand("브랜드D")
                .build()));

        cloths.add(clothRepository.save(Cloth.builder()
                .name("검은색 슬랙스")
                .wearNum(8)
                .visibility(Visibility.PUBLIC)
                .tempUpperBound(20)
                .tempLowerBound(10)
                .thicknessLevel(ThicknessLevel.LEVEL_0)
                .category(topCategory)
                .member(member3)
                .clothUrl("https://example.com/images/cloth5.jpg")
                .brand("브랜드E")
                .build()));

        cloths.add(clothRepository.save(Cloth.builder()
                .name("검은색 패딩")
                .wearNum(2)
                .visibility(Visibility.PRIVATE)
                .tempUpperBound(5)
                .tempLowerBound(-5)
                .thicknessLevel(ThicknessLevel.LEVEL_0)
                .category(topCategory)
                .member(member3)
                .clothUrl("https://example.com/images/cloth6.jpg")
                .brand("브랜드F")
                .build()));

        cloths.add(clothRepository.save(Cloth.builder()
                .name("빨간색 니트")
                .wearNum(4)
                .visibility(Visibility.PRIVATE)
                .tempUpperBound(18)
                .tempLowerBound(8)
                .thicknessLevel(ThicknessLevel.LEVEL_0)
                .category(topCategory)
                .member(member4)
                .clothUrl("https://example.com/images/cloth7.jpg")
                .brand("브랜드G")
                .build()));

        cloths.add(clothRepository.save(Cloth.builder()
                .name("파란색 청바지")
                .wearNum(12)
                .visibility(Visibility.PRIVATE)
                .tempUpperBound(15)
                .tempLowerBound(5)
                .thicknessLevel(ThicknessLevel.LEVEL_0)
                .category(topCategory)
                .member(member4)
                .clothUrl("https://example.com/images/cloth8.jpg")
                .brand("브랜드H")
                .build()));

        cloths.add(clothRepository.save(Cloth.builder()
                .name("흰색 후드티")
                .wearNum(6)
                .visibility(Visibility.PUBLIC)
                .tempUpperBound(22)
                .tempLowerBound(12)
                .thicknessLevel(ThicknessLevel.LEVEL_0)
                .category(topCategory)
                .member(member5)
                .clothUrl("https://example.com/images/cloth9.jpg")
                .brand("브랜드I")
                .build()));

        cloths.add(clothRepository.save(Cloth.builder()
                .name("검은색 자켓")
                .wearNum(5)
                .visibility(Visibility.PRIVATE)
                .tempUpperBound(10)
                .tempLowerBound(0)
                .thicknessLevel(ThicknessLevel.LEVEL_0)
                .category(topCategory)
                .member(member5)
                .clothUrl("https://example.com/images/cloth10.jpg")
                .brand("브랜드J")
                .build()));

        cloths.add(clothRepository.save(Cloth.builder()
                .name("흑청바지")
                .wearNum(0)
                .visibility(Visibility.PUBLIC)
                .tempUpperBound(15)
                .tempLowerBound(5)
                .thicknessLevel(ThicknessLevel.LEVEL_0)
                .category(topCategory)
                .member(member1)
                .clothUrl("https://example.com/images/cloth11.jpg")
                .brand("브랜드B")
                .build()));


        clothImageRepository.saveAll(List.of(
                new ClothImage(null, "https://example.com/images/cloth1_1.jpg", cloths.get(0)),
                new ClothImage(null, "https://example.com/images/cloth2_1.jpg", cloths.get(1)),
                new ClothImage(null, "https://example.com/images/cloth3_1.jpg", cloths.get(2)),
                new ClothImage(null, "https://example.com/images/cloth4_1.jpg", cloths.get(3)),
                new ClothImage(null, "https://example.com/images/cloth5_1.jpg", cloths.get(4)),
                new ClothImage(null, "https://example.com/images/cloth6_1.jpg", cloths.get(5)),
                new ClothImage(null, "https://example.com/images/cloth7_1.jpg", cloths.get(6)),
                new ClothImage(null, "https://example.com/images/cloth8_1.jpg", cloths.get(7))
        ));

        historyClothRepository.saveAll(List.of(
                HistoryCloth.builder().history(h1).cloth(cloths.get(0)).build(),
                HistoryCloth.builder().history(h2).cloth(cloths.get(1)).build(),
                HistoryCloth.builder().history(h3).cloth(cloths.get(2)).build(),
                HistoryCloth.builder().history(h4).cloth(cloths.get(3)).build(),
                HistoryCloth.builder().history(h5).cloth(cloths.get(4)).build(),
                HistoryCloth.builder().history(h6).cloth(cloths.get(5)).build(),
                HistoryCloth.builder().history(h7).cloth(cloths.get(6)).build(),
                HistoryCloth.builder().history(h8).cloth(cloths.get(7)).build(),
                HistoryCloth.builder().history(h9).cloth(cloths.get(8)).build(),
                HistoryCloth.builder().history(h10).cloth(cloths.get(9)).build()
        ));

        historyImageRepository.saveAll(List.of(
                HistoryImage.builder().imageUrl("https://example.com/images/new_year.jpg").history(h1).build(),
                HistoryImage.builder().imageUrl("https://example.com/images/reading.jpg").history(h2).build(),
                HistoryImage.builder().imageUrl("https://example.com/images/travel.jpg").history(h3).build(),
                HistoryImage.builder().imageUrl("https://example.com/images/food.jpg").history(h4).build(),
                HistoryImage.builder().imageUrl("https://example.com/images/christmas.jpg").history(h5).build(),
                HistoryImage.builder().imageUrl("https://example.com/imagefor6.jpg").history(h6).build(),
                HistoryImage.builder().imageUrl("https://example.com/images/movie.jpg").history(h7).build(),
                HistoryImage.builder().imageUrl("https://example.com/images/library.jpg").history(h9).build(),
                HistoryImage.builder().imageUrl("https://example.com/images/bookclub.jpg").history(h10).build()
        ));

    }

    @DisplayName("월별 기록을 정확하게 조회할 수 있다.")
    @Test
    void 월별_기록_조회() {
        // given
        Long memberId = 1L;
        String yearMonth = "2025-01";

        // when
        List<HistoryProjectionDTO> monthlyHistoryProjectionDTOS = historyProjectionRepository.getMonthlyHistoriesByMemberAndYearMonth(memberId, yearMonth);

        // then
        assertThat(monthlyHistoryProjectionDTOS.size()).isEqualTo(2);

        assertThat(monthlyHistoryProjectionDTOS.stream()
                .map(HistoryProjectionDTO::getId)
                .toList()).isEqualTo(List.of(1L, 2L));
    }

    @DisplayName("존재하지 않는 월별 기록은 아무것도 반환하지 않는다")
    @Test
    void 존재하지_않는_월별_기록_조회() {
        // given
        Long memberId = 1L;
        String yearMonth = "2050-06";

        // when
        List<HistoryProjectionDTO> monthlyHistoryProjectionDTOS = historyProjectionRepository.getMonthlyHistoriesByMemberAndYearMonth(memberId, yearMonth);

        // then
        assertThat(monthlyHistoryProjectionDTOS.size()).isEqualTo(0);
    }

    @DisplayName("기록에 등록된 옷들을 반환한다.")
    @Test
    void 특정_기록에_등록된_옷_조회() {
        // given
        Long historyId = 2L;
        Long historyId1ClothId = 1L;

        // when
        List<DailyHistoryClothProjectionDTO> cloths = historyProjectionRepository.findClothesByHistoryId(1L);

        // then
        assertThat(cloths.size()).isEqualTo(1L);
        assertThat(cloths.get(0).getClothImageUrl()).isEqualTo("https://example.com/images/cloth1_1.jpg");
        assertThat(cloths.get(0).getClothName()).isEqualTo("흰색 맨투맨");
        assertThat(cloths.get(0).getClothId()).isEqualTo(1L);
        assertThat(cloths.get(0).getVisibility()).isEqualTo(Visibility.PUBLIC);
    }

    @DisplayName("기록에 등록된 댓글을 반환한다")
    @Test
    void 특정_기록에_등록된_댓글_조회() {
        // given
        Long historyId = 1L;
        int page = 0;
        int pageSize = 10;

        // when
        List<HistoryCommentProjectionDTO> commentProjectionDTOS = historyProjectionRepository.findFlatCommentsByHistoryId(historyId, page, pageSize);

        // then
        assertThat(commentProjectionDTOS.size()).isEqualTo(2);

        HistoryCommentProjectionDTO dto1 = commentProjectionDTOS.get(0);
        HistoryCommentProjectionDTO dto2 = commentProjectionDTOS.get(1);

        // 댓글 1 내용 점검
        assertThat(dto1.getCommentId()).isEqualTo(1L);
        assertThat(dto1.isRoot()).isEqualTo(true);
        assertThat(dto1.getParentId()).isEqualTo(null);
        assertThat(dto1.getClokeyId()).isEqualTo("clokey1");
        assertThat(dto1.getNickname()).isEqualTo("User1");
        assertThat(dto1.getProfileImageUrl()).isEqualTo("https://example.com/user1.png");

        // 댓글 2 내용 점검
        assertThat(dto2.getCommentId()).isEqualTo(6L);
        assertThat(dto2.isRoot()).isEqualTo(false);
        assertThat(dto2.getParentId()).isEqualTo(1L);
        assertThat(dto2.getClokeyId()).isEqualTo("clokey2");
        assertThat(dto2.getNickname()).isEqualTo("User2");
        assertThat(dto2.getProfileImageUrl()).isEqualTo("https://example.com/user2.png");

    }

    @DisplayName("특정 회원이 좋아요한 기록들을 작성자와 함께 조회할 수 있다.")
    @Test
    void 좋아요_기록_작성자와_함께_조회() {

        // given
        Long memberId = 2L;
        int pageSize = 10;

        // when
        Page<HistoryProjectionDTO> historyProjectionDTOS = historyProjectionRepository.findLikedHistoryAndAuthorIds(memberId, Pageable.ofSize(pageSize));
        List<HistoryProjectionDTO> result = historyProjectionDTOS.getContent();

        // then
        assertThat(result.size()).isEqualTo(2);

        HistoryProjectionDTO dto1 = result.get(0);
        HistoryProjectionDTO dto2 = result.get(1);

        // 첫 번째 history 내용 점검
        assertThat(dto1.getId()).isEqualTo(2L);
        assertThat(dto1.getMemberId()).isEqualTo(1L);

        // 두 번째 history 내용 점검
        assertThat(dto2.getId()).isEqualTo(3L);
        assertThat(dto2.getMemberId()).isEqualTo(2L);
    }
}


