package com.dduru.gildongmu.recommendation.service;

import com.dduru.gildongmu.auth.exception.UserNotFoundException;
import com.dduru.gildongmu.recommendation.domain.MateRecommendation;
import com.dduru.gildongmu.recommendation.domain.MateRecommendationPass;
import com.dduru.gildongmu.recommendation.exception.MateRecommendationNotFoundException;
import com.dduru.gildongmu.recommendation.repository.MateRecommendationPassRepository;
import com.dduru.gildongmu.recommendation.repository.MateRecommendationRepository;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MateRecommendationPassService {

    private final MateRecommendationRepository recommendationRepository;
    private final MateRecommendationPassRepository passRepository;
    private final UserRepository userRepository;

    @Transactional
    public void pass(Long userId, Long recommendationId) {
        userRepository.findByIdWithLock(userId).orElseThrow(UserNotFoundException::new);

        MateRecommendation recommendation = recommendationRepository
                .findOwnedByIdWithLock(recommendationId, userId)
                .orElseThrow(MateRecommendationNotFoundException::new);

        Long postId = recommendation.getPost().getId();
        if (passRepository.existsByUser_IdAndPost_Id(userId, postId)) {
            return;
        }

        passRepository.save(MateRecommendationPass.of(
                recommendation.getBatch().getUser(),
                recommendation.getPost()
        ));
    }
}
