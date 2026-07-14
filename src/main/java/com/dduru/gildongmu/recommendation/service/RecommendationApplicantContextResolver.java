package com.dduru.gildongmu.recommendation.service;

import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.profile.exception.ProfileNotFoundException;
import com.dduru.gildongmu.recommendation.domain.UserRecommendationAvailableDate;
import com.dduru.gildongmu.recommendation.dto.query.ApplicantRecommendationQueryResult;
import com.dduru.gildongmu.recommendation.dto.query.DestinationPreferenceFilter;
import com.dduru.gildongmu.recommendation.dto.query.DestinationPreferenceFilterRow;
import com.dduru.gildongmu.recommendation.dto.query.RecommendationApplicantContext;
import com.dduru.gildongmu.recommendation.exception.RecommendationTendencyMissingException;
import com.dduru.gildongmu.recommendation.repository.ApplicantRecommendationQueryRepository;
import com.dduru.gildongmu.recommendation.repository.UserRecommendationAvailableDateRepository;
import com.dduru.gildongmu.recommendation.repository.UserRecommendationDestinationPreferenceRepository;
import com.dduru.gildongmu.recommendation.support.AvailableDateRange;
import com.dduru.gildongmu.recommendation.support.RecommendationAgeCalculator;
import com.dduru.gildongmu.recommendation.support.TravelTendencyScores;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RecommendationApplicantContextResolver {

    private final TimeProvider timeProvider;
    private final ApplicantRecommendationQueryRepository applicantRecommendationQueryRepository;
    private final UserRecommendationDestinationPreferenceRepository destinationPreferenceRepository;
    private final UserRecommendationAvailableDateRepository availableDateRepository;

    @Transactional(readOnly = true)
    public Optional<RecommendationApplicantContext> resolve(Long userId) {
        Optional<ApplicantRecommendationQueryResult> applicantQueryResult =
                applicantRecommendationQueryRepository.findApplicantContext(userId);

        if (applicantQueryResult.isEmpty() || !applicantQueryResult.get().isSurveyCompleted()) {
            return Optional.empty();
        }

        ApplicantRecommendationQueryResult applicant = applicantQueryResult.get();
        if (!applicant.hasProfile()) {
            throw new ProfileNotFoundException();
        }
        if (!applicant.hasTravelTendency()) {
            throw new RecommendationTendencyMissingException();
        }

        LocalDate today = timeProvider.today();
        return Optional.of(new RecommendationApplicantContext(
                today,
                applicant.gender(),
                RecommendationAgeCalculator.calculate(applicant.birthday(), today),
                destinationPreferenceFilter(userId),
                availableDateRanges(userId),
                applicantScores(applicant)
        ));
    }

    private DestinationPreferenceFilter destinationPreferenceFilter(Long userId) {
        List<DestinationPreferenceFilterRow> rows = destinationPreferenceRepository.findFilterRowsByUserId(userId);
        return DestinationPreferenceFilter.from(rows);
    }

    private List<AvailableDateRange> availableDateRanges(Long userId) {
        List<UserRecommendationAvailableDate> availableDates = availableDateRepository.findAllByUser_Id(userId);
        return AvailableDateRange.from(availableDates);
    }

    private TravelTendencyScores applicantScores(ApplicantRecommendationQueryResult applicant) {
        return new TravelTendencyScores(
                toDouble(applicant.rhythmScore()),
                toDouble(applicant.energyScore()),
                toDouble(applicant.consumptionScore()),
                toDouble(applicant.decisionScore())
        );
    }

    private double toDouble(BigDecimal value) {
        return value.doubleValue();
    }
}
