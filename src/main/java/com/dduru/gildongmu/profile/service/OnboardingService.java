package com.dduru.gildongmu.profile.service;

import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.SurveyStatus;
import com.dduru.gildongmu.profile.dto.response.OnboardingStatusResponse;
import com.dduru.gildongmu.profile.repository.ProfileRepository;
import com.dduru.gildongmu.survey.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final ProfileRepository profileRepository;
    private final SurveyRepository surveyRepository;

    @Transactional
    public OnboardingStatusResponse getOnboardingStatus(Long userId) {
        Profile profile = profileRepository.getByUserIdOrThrow(userId);

        boolean isSurveyCompleted = surveyRepository.existsByUser_Id(userId);
        SurveyStatus surveyStatus = profile.getSurveyStatus();

        // 데이터 정합성 검증: 설문 완료 상태인데 스킵 상태인 경우 자동 보정
        if (isSurveyCompleted && surveyStatus == SurveyStatus.SKIPPED) {
            log.warn("온보딩 데이터 정합성 오류 감지 - userId: {}, surveyCompleted=true & surveyStatus=SKIPPED → COMPLETED로 자동 보정", userId);
            profile.completeSurvey();
            surveyStatus = SurveyStatus.COMPLETED;
        }

        return new OnboardingStatusResponse(
                profile.isOnboardingCompleted(),
                profile.isProfileCompleted(),
                surveyStatus
        );
    }
}
