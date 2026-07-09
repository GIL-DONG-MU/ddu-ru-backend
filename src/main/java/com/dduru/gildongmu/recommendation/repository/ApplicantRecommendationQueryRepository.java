package com.dduru.gildongmu.recommendation.repository;

import com.dduru.gildongmu.recommendation.dto.query.ApplicantRecommendationQueryResult;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.dduru.gildongmu.onboarding.domain.QUserOnboarding.userOnboarding;
import static com.dduru.gildongmu.profile.domain.QProfile.profile;
import static com.dduru.gildongmu.survey.domain.QTravelTendency.travelTendency;
import static com.dduru.gildongmu.user.domain.QUser.user;

@Repository
@RequiredArgsConstructor
public class ApplicantRecommendationQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Optional<ApplicantRecommendationQueryResult> findApplicantContext(Long userId) {
        return Optional.ofNullable(queryFactory
                .select(Projections.constructor(
                        ApplicantRecommendationQueryResult.class,
                        userOnboarding.surveyStatus,
                        profile.id,
                        profile.gender,
                        profile.birthday,
                        travelTendency.id,
                        travelTendency.rhythmScore,
                        travelTendency.energyScore,
                        travelTendency.consumptionScore,
                        travelTendency.decisionScore
                ))
                .from(user)
                .leftJoin(userOnboarding).on(userOnboarding.user.eq(user))
                .leftJoin(profile).on(profile.user.eq(user))
                .leftJoin(travelTendency).on(travelTendency.user.eq(user))
                .where(user.id.eq(userId))
                .fetchOne());
    }
}
