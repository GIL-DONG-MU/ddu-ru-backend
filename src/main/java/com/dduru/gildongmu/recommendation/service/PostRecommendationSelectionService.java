package com.dduru.gildongmu.recommendation.service;

import com.dduru.gildongmu.recommendation.domain.RecommendationPolicy;
import com.dduru.gildongmu.recommendation.dto.query.RecommendablePostQueryResult;
import com.dduru.gildongmu.recommendation.dto.query.RecommendationApplicantContext;
import com.dduru.gildongmu.recommendation.dto.result.PostRecommendationResult;
import com.dduru.gildongmu.recommendation.dto.result.ScoredPostRecommendation;
import com.dduru.gildongmu.recommendation.repository.RecommendablePostQueryRepository;
import com.dduru.gildongmu.recommendation.support.RecommendationAvailableDateMatcher;
import com.dduru.gildongmu.recommendation.support.RecommendationScore;
import com.dduru.gildongmu.recommendation.support.RecommendationScoreCalculator;
import com.dduru.gildongmu.recommendation.support.TravelTendencyScores;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostRecommendationSelectionService {

    private final RecommendationApplicantContextResolver applicantContextResolver;
    private final RecommendablePostQueryRepository recommendablePostQueryRepository;
    private final RecommendationAvailableDateMatcher availableDateMatcher;
    private final RecommendationScoreCalculator scoreCalculator;

    @Transactional(readOnly = true)
    public PostRecommendationResult selectRecommendations(Long userId) {
        Optional<RecommendationApplicantContext> context = applicantContextResolver.resolve(userId);
        if (context.isEmpty()) {
            return PostRecommendationResult.surveyRequired();
        }
        return selectRecommendations(userId, context.get());
    }

    @Transactional(readOnly = true)
    public PostRecommendationResult selectRecommendations(
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

}
