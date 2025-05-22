package com.clokey.server.domain.history.domain.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HistoryProjectionRepositoryImpl implements HistoryProjectionRepository {

    private final JPAQueryFactory queryFactory;

}
