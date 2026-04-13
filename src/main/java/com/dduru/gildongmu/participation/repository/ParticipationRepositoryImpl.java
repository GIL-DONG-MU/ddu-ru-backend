package com.dduru.gildongmu.participation.repository;

import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import com.dduru.gildongmu.participation.dto.query.ParticipationRetrieveQueryResult;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.dduru.gildongmu.participation.domain.QParticipation.participation;
import static com.dduru.gildongmu.post.domain.QPost.post;
import static com.dduru.gildongmu.profile.domain.QProfile.profile;
import static com.dduru.gildongmu.survey.domain.QAvatarProfile.avatarProfile;
import static com.dduru.gildongmu.user.domain.QUser.user;

@Repository
@RequiredArgsConstructor
public class ParticipationRepositoryImpl implements ParticipationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ParticipationRetrieveQueryResult> findReceivedRequestsByStatus(
            Long postOwnerId,
            ParticipationStatus status
    ) {
        return queryFactory
                .select(Projections.constructor(
                        ParticipationRetrieveQueryResult.class,
                        participation.id,
                        user.id,
                        user.name,
                        profile.profileImageType,
                        profile.uploadedImageUrl,
                        avatarProfile.imageUrl,
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
                // nullable 연관을 타면 implicit join이 inner join처럼 동작해서 avatar가 없는 유저가 결과에서 빠질 수 있어서 명시적으로 left join으로 profile 가져옴.
                .leftJoin(user.profile, profile)
                .leftJoin(profile.avatar, avatarProfile)
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
