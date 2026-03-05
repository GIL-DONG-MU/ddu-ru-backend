package com.dduru.gildongmu.onboarding.service;

import com.dduru.gildongmu.onboarding.domain.UserOnboarding;
import com.dduru.gildongmu.onboarding.dto.response.OnboardingStatusResponse;
import com.dduru.gildongmu.onboarding.repository.UserOnboardingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final UserOnboardingRepository userOnboardingRepository;

    @Transactional(readOnly = true)
    public OnboardingStatusResponse getStatus(Long userId) {
        return OnboardingStatusResponse.from(getUserOnboarding(userId));
    }

    @Transactional
    public void completeOnboarding(Long userId) {
        getUserOnboarding(userId).completeOnboarding();
        log.info("유저의 개인정보 입력이 완료되었습니다. {}", userId);
    }

    @Transactional
    public void completeSurvey(Long userId) {
        getUserOnboarding(userId).completeSurvey();
        log.info("유저의 설문조사가 완료되었습니다. {}", userId);
    }

    @Transactional
    public void skipSurvey(Long userId) {
        getUserOnboarding(userId).skipSurvey();
        log.info("유저의 설문조사가 스킵되었습니다. {}", userId);
    }

    @Transactional
    public void completeProfile(Long userId) {
        getUserOnboarding(userId).completeProfile();
        log.info("유저의 프로필 설정이 완료되었습니다. {}", userId);
    }

    private UserOnboarding getUserOnboarding(Long userId) {
        return userOnboardingRepository.getByUserIdOrThrow(userId);
    }
}
