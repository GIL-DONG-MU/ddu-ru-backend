package com.dduru.gildongmu.profile.service;

import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.dto.response.OnboardingStatusResponse;
import com.dduru.gildongmu.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OnboardingService {

    private final ProfileRepository profileRepository;

    public OnboardingStatusResponse getOnboardingStatus(Long userId) {
        Profile profile = profileRepository.getByUserIdOrThrow(userId);

        return new OnboardingStatusResponse(
                profile.isOnboardingCompleted(),
                profile.isProfileCompleted(),
                profile.getSurveyStatus()
        );
    }
}
