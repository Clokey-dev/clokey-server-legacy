package com.clokey.server.domain.history.domain.repository;

import com.clokey.server.domain.category.domain.entity.Category;
import com.clokey.server.domain.category.domain.repostiory.CategoryRepository;
import com.clokey.server.domain.cloth.domain.entity.Cloth;
import com.clokey.server.domain.cloth.domain.entity.ClothImage;
import com.clokey.server.domain.cloth.domain.repository.ClothImageRepository;
import com.clokey.server.domain.cloth.domain.repository.ClothRepository;
import com.clokey.server.domain.history.domain.entity.*;
import com.clokey.server.domain.member.domain.entity.Member;
import com.clokey.server.domain.member.domain.repository.MemberRepository;
import com.clokey.server.domain.model.entity.enums.MemberStatus;
import com.clokey.server.domain.model.entity.enums.SocialType;
import com.clokey.server.domain.model.entity.enums.ThicknessLevel;
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
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(QuerydslConfig.class)
class HistoryClothRepositoryTest {

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
    private CategoryRepository categoryRepository;

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

    @DisplayName("특정 옷 ID를 기준으로 모든 기록-옷을 삭제합니다.")
    @Test
    void 특정_옷_기록_옷_삭제() {
        // given
        Long clothId = 1L;

        boolean existCheck = historyClothRepository.existsByClothId(clothId);
        assertThat(existCheck).isTrue();

        // when
        historyClothRepository.deleteAllByClothId(clothId);

        // then
        boolean newExistCheck = historyClothRepository.existsByClothId(clothId);
        assertThat(newExistCheck).isFalse();
    }

    @DisplayName("특정 기록과 옷을 기준으로 삭제합니다")
    @Test
    void 특정_기록_옷_기준_삭제() {
        // given
        History history = historyRepository.findById(1L).get();
        Cloth cloth = clothRepository.findById(1L).get();

        boolean existCheck = historyClothRepository.existsByHistoryIdAndClothId(1L, 1L);
        assertThat(existCheck).isTrue();

        // when
        historyClothRepository.deleteByHistoryAndCloth(history, cloth);

        // then
        boolean newExistCheck = historyClothRepository.existsByHistoryIdAndClothId(1L, 1L);
        assertThat(newExistCheck).isFalse();
    }

    @DisplayName("기록 Id를 기준으로 옷들의 Id를 찾을 수 있다.")
    @Test
    void 기록에_등록된_옷_ID_찿기() {
        // given
        Long historyId = 1L;

        // when
        List<Long> ids = historyClothRepository.findClothIdsByHistoryId(historyId);

        // then
        assertThat(ids).isEqualTo(List.of(1L));
    }

    @DisplayName("기록 Id를 기준으로 옷들을 찾을 수 있다.")
    @Test
    void 기록에_등록된_옷__찿기() {
        // given
        Long historyId = 1L;

        // when
        List<Cloth> cloths = historyClothRepository.findAllClothsByHistoryId(historyId);

        // then
        assertThat(cloths.size()).isEqualTo(1);
        assertThat(cloths.get(0).getId()).isEqualTo(1L);
    }

    @DisplayName("특정 기록에 등록된 기록-옷 전부 삭제")
    @Test
    void 특정_기록의_기록_옷_전부_삭제() {
        // given
        Long historyId = 1L;

        boolean existCheck = historyClothRepository.existsByHistoryId(historyId);
        assertThat(existCheck).isTrue();

        // when
        historyClothRepository.deleteAllByHistoryId(historyId);

        // then
        boolean newExistCheck = historyClothRepository.existsByHistoryId(historyId);
        assertThat(newExistCheck).isFalse();
    }

    @DisplayName("기록들에 등록된 기록-옷 전부 삭제")
    @Test
    void 기록들에_등록된_기록_옷_전부_삭제() {
        // given
        List<Long> ids = List.of(1L, 2L);

        boolean existCheck1 = historyClothRepository.existsByHistoryId(1L);
        boolean existCheck2 = historyClothRepository.existsByHistoryId(2L);
        assertThat(existCheck1).isTrue();
        assertThat(existCheck2).isTrue();

        // when
        historyClothRepository.deleteAllByHistoryIds(ids);

        // then
        boolean newExistCheck1 = historyClothRepository.existsByHistoryId(1L);
        boolean newExistCheck2 = historyClothRepository.existsByHistoryId(2L);
        assertThat(newExistCheck1).isFalse();
        assertThat(newExistCheck2).isFalse();

    }
}

