package com.clokey.server.domain.cloth.domain.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ClothProjectionRepositoryImpl implements ClothProjectionRepository{

    private final JPAQueryFactory queryFactory;

}
