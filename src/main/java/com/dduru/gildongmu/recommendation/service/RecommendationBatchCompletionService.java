package com.dduru.gildongmu.recommendation.service;

import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.recommendation.domain.MateRecommendation;
import com.dduru.gildongmu.recommendation.domain.MateRecommendationBatch;
import com.dduru.gildongmu.recommendation.dto.result.PostRecommendationResult;
import com.dduru.gildongmu.recommendation.dto.result.PostRecommendationResultStatus;
import com.dduru.gildongmu.recommendation.dto.result.ScoredPostRecommendation;
import com.dduru.gildongmu.recommendation.repository.MateRecommendationBatchRepository;
import com.dduru.gildongmu.recommendation.repository.MateRecommendationRepository;
import com.dduru.gildongmu.recommendation.support.RecommendationReasonJsonConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class RecommendationBatchCompletionService {

    private final MateRecommendationBatchRepository batchRepository;
    private final MateRecommendationRepository recommendationRepository;
    private final PostRepository postRepository;
    private final RecommendationReasonJsonConverter reasonJsonConverter;

    @Transactional
    public void complete(Long batchId, PostRecommendationResult result) {
        MateRecommendationBatch batch = batchRepository.findByIdForUpdate(batchId)
                .orElseThrow(() -> new IllegalStateException("Recommendation batch not found: " + batchId));

        if (result.status() == PostRecommendationResultStatus.NO_CANDIDATES) {
            batch.markEmpty();
            return;
        }
        if (result.status() != PostRecommendationResultStatus.READY) {
            throw new IllegalStateException("Cannot complete batch with result status: " + result.status());
        }

        List<ScoredPostRecommendation> scored = result.recommendations();
        if (scored.isEmpty()) {
            batch.markEmpty();
            return;
        }
        Map<Long, Post> postsById = postRepository.findAllById(
                        scored.stream().map(ScoredPostRecommendation::postId).toList()
                ).stream()
                .collect(Collectors.toMap(Post::getId, Function.identity()));

        List<MateRecommendation> recommendations = IntStream.range(0, scored.size())
                .mapToObj(index -> toEntity(batch, scored.get(index), postsById, index + 1))
                .toList();
        recommendationRepository.saveAll(recommendations);
        batch.complete();
    }

    private MateRecommendation toEntity(
            MateRecommendationBatch batch,
            ScoredPostRecommendation scored,
            Map<Long, Post> postsById,
            int rank
    ) {
        Post post = postsById.get(scored.postId());
        if (post == null) {
            throw new IllegalStateException("Recommended post not found: " + scored.postId());
        }
        return MateRecommendation.create(
                batch,
                post,
                rank,
                scored.matchPercentage(),
                reasonJsonConverter.toJson(scored.matchReasons()),
                reasonJsonConverter.toJson(scored.cautionPoints())
        );
    }
}
