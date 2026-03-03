package com.dduru.gildongmu.onboarding.service;

import com.dduru.gildongmu.onboarding.domain.UserOnboarding;
import com.dduru.gildongmu.onboarding.dto.response.OnboardingStatusResponse;
import com.dduru.gildongmu.onboarding.repository.UserOnboardingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final UserOnboardingRepository userOnboardingRepository;

    @Transactional(readOnly = true)
    public OnboardingStatusResponse getStatus(Long userId) {
        UserOnboarding userOnboarding = userOnboardingRepository.getByUserIdOrThrow(userId);

        return OnboardingStatusResponse.from(userOnboarding);
    }
}
