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
}
