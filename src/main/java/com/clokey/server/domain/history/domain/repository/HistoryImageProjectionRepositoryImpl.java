package com.clokey.server.domain.history.domain.repository;

import com.clokey.server.domain.history.domain.entity.QHistoryImage;
import com.clokey.server.domain.history.dto.projection.HistoryImageProjectionDTO;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class HistoryImageProjectionRepositoryImpl implements HistoryImageProjectionRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<String> getFirstImageUrlsOfHistories(List<Long> historyIds) {
        QHistoryImage hi = QHistoryImage.historyImage;
        QHistoryImage hiSub = new QHistoryImage("hiSub");

        List<HistoryImageProjectionDTO> result = queryFactory
                .select(Projections.constructor(
                        HistoryImageProjectionDTO.class,
                        hi.id,
                        hi.imageUrl,
                        hi.history.id
                ))
                .from(hi)
                .where(
                        hi.history.id.in(historyIds),
                        hi.createdAt.eq(
                                JPAExpressions
                                        .select(hiSub.createdAt.min())
                                        .from(hiSub)
                                        .where(hiSub.history.id.eq(hi.history.id))
                        )
                )
                .fetch();

        // Step 2. Map으로 매핑
        Map<Long, HistoryImageProjectionDTO> map = result.stream()
                .collect(Collectors.toMap(HistoryImageProjectionDTO::getHistoryId, Function.identity()));

        return historyIds.stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .map(HistoryImageProjectionDTO::getImageUrl)
                .collect(Collectors.toList());
    }
}
