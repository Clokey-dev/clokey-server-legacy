package com.clokey.server.domain.history.domain.repository;

import com.clokey.server.domain.cloth.domain.entity.QCloth;
import com.clokey.server.domain.cloth.domain.entity.QClothImage;
import com.clokey.server.domain.history.domain.entity.QHistory;
import com.clokey.server.domain.history.domain.entity.QHistoryCloth;
import com.clokey.server.domain.history.dto.projection.DailyHistoryClothProjectionDTO;
import com.clokey.server.domain.history.dto.projection.HistoryProjectionDTO;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RequiredArgsConstructor
public class HistoryProjectionRepositoryImpl implements HistoryProjectionRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<HistoryProjectionDTO> getMontlyHistoriesByMemberAndYearMonth(Long memberId, String yearMonth) {
        QHistory history = QHistory.history;

        //입력 YYYY-MM을 기준으로 해당 달의 1일 부터 다음 달의 1일 전까지 범위 쿼리 수행
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.plusMonths(1).atDay(1);

        return queryFactory
                .select(Projections.fields(
                        HistoryProjectionDTO.class,
                        history.id.as("id"),
                        history.historyDate.as("historyDate"),
                        history.visibility.as("visibility")
                ))
                .from(history)
                .where(
                        history.member.id.eq(memberId),
                        history.historyDate.goe(startDate),
                        history.historyDate.lt(endDate)
                )
                .fetch();
    }

    public List<DailyHistoryClothProjectionDTO> findClothesByHistoryId(Long historyId) {
        QHistoryCloth hc = QHistoryCloth.historyCloth;
        QCloth cloth = QCloth.cloth;
        QClothImage image = QClothImage.clothImage;

        List<Long> clothIds = queryFactory
                .select(hc.cloth.id)
                .from(hc)
                .where(hc.history.id.eq(historyId))
                .fetch();

        if (clothIds.isEmpty()) {
            return List.of(); // 조기 반환 : 에러로 대체해야함. -> 버그 데이터 거든요.
        }

        // 2단계: cloth + clothImage 조회
        return queryFactory
                .select(Projections.constructor(
                        DailyHistoryClothProjectionDTO.class,
                        cloth.id,
                        image.imageUrl,
                        cloth.name,
                        cloth.visibility
                ))
                .from(cloth)
                .leftJoin(cloth.image, image)
                .where(cloth.id.in(clothIds))
                .fetch();
    }
}
