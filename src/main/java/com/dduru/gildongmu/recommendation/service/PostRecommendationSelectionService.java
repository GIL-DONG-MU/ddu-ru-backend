package com.dduru.gildongmu.recommendation.service;

import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.onboarding.domain.UserOnboarding;
import com.dduru.gildongmu.onboarding.domain.enums.SurveyStatus;
import com.dduru.gildongmu.onboarding.repository.UserOnboardingRepository;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.exception.ProfileNotFoundException;
import com.dduru.gildongmu.profile.repository.ProfileRepository;
import com.dduru.gildongmu.recommendation.domain.RecommendationPolicy;
import com.dduru.gildongmu.recommendation.domain.UserRecommendationAvailableDate;
import com.dduru.gildongmu.recommendation.dto.query.DestinationPreferenceFilter;
import com.dduru.gildongmu.recommendation.dto.query.DestinationPreferenceFilterRow;
import com.dduru.gildongmu.recommendation.dto.query.RecommendablePostQueryResult;
import com.dduru.gildongmu.recommendation.dto.result.PostRecommendationResult;
import com.dduru.gildongmu.recommendation.dto.result.ScoredPostRecommendation;
import com.dduru.gildongmu.recommendation.exception.RecommendationTendencyMissingException;
import com.dduru.gildongmu.recommendation.repository.RecommendablePostQueryRepository;
import com.dduru.gildongmu.recommendation.repository.UserRecommendationAvailableDateRepository;
import com.dduru.gildongmu.recommendation.repository.UserRecommendationDestinationPreferenceRepository;
import com.dduru.gildongmu.recommendation.support.AvailableDateRange;
import com.dduru.gildongmu.recommendation.support.RecommendationAgeCalculator;
import com.dduru.gildongmu.recommendation.support.RecommendationAvailableDateMatcher;
import com.dduru.gildongmu.recommendation.support.RecommendationScore;
import com.dduru.gildongmu.recommendation.support.RecommendationScoreCalculator;
import com.dduru.gildongmu.recommendation.support.TravelTendencyScores;
import com.dduru.gildongmu.survey.domain.TravelTendency;
import com.dduru.gildongmu.survey.repository.TravelTendencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostRecommendationSelectionService {

    private final TimeProvider timeProvider;
    private final UserOnboardingRepository userOnboardingRepository;
    private final ProfileRepository profileRepository;
    private final TravelTendencyRepository travelTendencyRepository;
    private final UserRecommendationDestinationPreferenceRepository destinationPreferenceRepository;
    private final UserRecommendationAvailableDateRepository availableDateRepository;
    private final RecommendablePostQueryRepository recommendablePostQueryRepository;
    private final RecommendationAvailableDateMatcher availableDateMatcher;
    private final RecommendationScoreCalculator scoreCalculator;

    @Transactional(readOnly = true)
    public PostRecommendationResult selectRecommendations(Long userId) {
        Optional<UserOnboarding> onboarding = userOnboardingRepository.findByUser_Id(userId);
        if (onboarding.isEmpty() || onboarding.get().getSurveyStatus() != SurveyStatus.COMPLETED) {
            return PostRecommendationResult.surveyRequired();
        }

        Optional<Profile> profile = profileRepository.findByUser_Id(userId);
        if (profile.isEmpty()) {
            throw new ProfileNotFoundException();
        }

        Optional<TravelTendency> applicantTendency = travelTendencyRepository.findByUser_Id(userId);
        if (applicantTendency.isEmpty()) {
            throw new RecommendationTendencyMissingException();
        }

        LocalDate today = timeProvider.today();

        RecommendationApplicantContext context = new RecommendationApplicantContext(
                today,
                profile.get().getGender(),
                RecommendationAgeCalculator.calculate(profile.get().getBirthday(), today),
                getDestinationPreferenceFilter(userId),
                availableDateRanges(userId),
                TravelTendencyScores.from(applicantTendency.get())
        );

        return selectRecommendablePosts(userId, context);
    }

    private PostRecommendationResult selectRecommendablePosts(
            Long userId,
            RecommendationApplicantContext context
    ) {
        List<RecommendablePostQueryResult> recommendablePosts = recommendablePostQueryRepository.findRecommendablePosts(
                userId,
                context.today(),
                context.gender(),
                context.age(),
                context.destinationPreferenceFilter()
        );

        List<ScoredPostRecommendation> topRecommendations = filterScoreAndSelectTopPosts(recommendablePosts, context);

        if (topRecommendations.isEmpty()) {
            return PostRecommendationResult.noCandidates();
        }

        return PostRecommendationResult.ready(topRecommendations);
    }

    private List<ScoredPostRecommendation> filterScoreAndSelectTopPosts(
            List<RecommendablePostQueryResult> posts,
            RecommendationApplicantContext context
    ) {
        return posts.stream()
                .filter(post -> availableDateMatcher.matches(
                        post.companionType(),
                        post.startDate(),
                        post.endDate(),
                        context.availableDateRanges()
                ))
                .map(post -> toScoredRecommendation(post, context.applicantScores()))
                .sorted(recommendationOrder())
                .limit(RecommendationPolicy.MAX_DAILY_RECOMMENDATIONS)
                .toList();
    }

    private DestinationPreferenceFilter getDestinationPreferenceFilter(Long userId) {
        List<DestinationPreferenceFilterRow> rows = destinationPreferenceRepository.findFilterRowsByUserId(userId);
        return DestinationPreferenceFilter.from(rows);
    }

    private List<AvailableDateRange> availableDateRanges(Long userId) {
        List<UserRecommendationAvailableDate> availableDates = availableDateRepository.findAllByUser_Id(userId);
        return AvailableDateRange.from(availableDates);
    }

    private ScoredPostRecommendation toScoredRecommendation(
            RecommendablePostQueryResult post,
            TravelTendencyScores applicantScores
    ) {
        RecommendationScore score = scoreCalculator.calculate(
                applicantScores,
                toHostScores(post)
        );

        return new ScoredPostRecommendation(
                post.postId(),
                post.startDate(),
                post.endDate(),
                score.matchPercentage(),
                score.matchReasons(),
                score.cautionPoints()
        );
    }

    private TravelTendencyScores toHostScores(RecommendablePostQueryResult post) {
        return new TravelTendencyScores(
                toDouble(post.hostRhythmScore()),
                toDouble(post.hostEnergyScore()),
                toDouble(post.hostConsumptionScore()),
                toDouble(post.hostDecisionScore())
        );
    }

    private double toDouble(BigDecimal value) {
        return value.doubleValue();
    }

    private Comparator<ScoredPostRecommendation> recommendationOrder() {
        return Comparator.comparingInt(ScoredPostRecommendation::matchPercentage).reversed()
                .thenComparing(ScoredPostRecommendation::startDate)
                .thenComparing(Comparator.comparing(ScoredPostRecommendation::postId).reversed());
    }

    private record RecommendationApplicantContext(
            LocalDate today,
            Gender gender,
            Integer age,
            DestinationPreferenceFilter destinationPreferenceFilter,
            List<AvailableDateRange> availableDateRanges,
            TravelTendencyScores applicantScores
    ) {
    }
}
