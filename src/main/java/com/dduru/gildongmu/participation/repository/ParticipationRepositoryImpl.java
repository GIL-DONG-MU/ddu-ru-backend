package com.dduru.gildongmu.participation.repository;

import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import com.dduru.gildongmu.participation.dto.response.ParticipationRetrieveResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.dduru.gildongmu.participation.domain.QParticipation.participation;
import static com.dduru.gildongmu.post.domain.QPost.post;
import static com.dduru.gildongmu.user.domain.QUser.user;

@Repository
@RequiredArgsConstructor
public class ParticipationRepositoryImpl implements ParticipationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ParticipationRetrieveResponse> findReceivedRequestsByStatus(
            Long postOwnerId,
            ParticipationStatus status
    ) {
        return queryFactory
                .select(Projections.constructor(
                        ParticipationRetrieveResponse.class,
                        participation.id,
                        user.id,
                        user.name,
                        participation.message,
                        participation.status,
                        participation.createdAt,
                        participation.contactedAt,
                        participation.approvedAt,
                        participation.rejectedAt,
                        post.id,
                        post.title
                ))
                .from(participation)
                .join(participation.user, user)
                .join(participation.post, post)
                .where(
                        post.user.id.eq(postOwnerId),
                        post.isDeleted.isFalse(),
                        statusCondition(status)
                )
                .orderBy(participation.createdAt.desc())
                .fetch();
    }

    private BooleanExpression statusCondition(ParticipationStatus status) {
        if (status == null) {
            return null;
        }
        return participation.status.eq(status);
    }
}
