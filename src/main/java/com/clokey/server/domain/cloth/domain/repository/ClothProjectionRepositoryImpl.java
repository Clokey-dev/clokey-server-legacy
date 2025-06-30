package com.clokey.server.domain.cloth.domain.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ClothProjectionRepositoryImpl implements ClothProjectionRepository{

    private final JPAQueryFactory queryFactory;

}
