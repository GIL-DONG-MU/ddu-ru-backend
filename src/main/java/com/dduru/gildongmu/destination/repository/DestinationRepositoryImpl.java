package com.dduru.gildongmu.destination.repository;

import com.dduru.gildongmu.destination.domain.Destination;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.dduru.gildongmu.destination.domain.QDestination.destination;

@Repository
@RequiredArgsConstructor
public class DestinationRepositoryImpl implements DestinationRepositoryCustom {

    private static final int SEARCH_RESULT_MAX_SIZE = 20;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Destination> searchByKeyword(String keyword) {
        return queryFactory
                .selectFrom(destination)
                .where(keywordCondition(keyword))
                .orderBy(destination.city.asc())
                .limit(SEARCH_RESULT_MAX_SIZE)
                .fetch();
    }

    private BooleanExpression keywordCondition(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        String trimmed = keyword.trim();
        return destination.city.containsIgnoreCase(trimmed)
                .or(destination.countryName.containsIgnoreCase(trimmed));
    }
}
