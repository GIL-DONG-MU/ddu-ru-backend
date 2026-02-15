package com.dduru.gildongmu.profile.service;

import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.dto.response.OnboardingStatusResponse;
import com.dduru.gildongmu.profile.repository.ProfileRepository;
import com.dduru.gildongmu.survey.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OnboardingService {

    private final ProfileRepository profileRepository;
    private final SurveyRepository surveyRepository;

    public OnboardingStatusResponse getOnboardingStatus(Long userId) {
        Profile profile = profileRepository.getByUserIdOrThrow(userId);

        boolean isBasicInfoCompleted = profile.getNickname() != null
                && profile.getGender() != null
                && profile.getPhoneNumber() != null
                && profile.getBirthday() != null;

        boolean isSurveyCompleted = surveyRepository.existsByUser_Id(userId);
        boolean isSurveySkipped = profile.isSurveySkipped();
        boolean isProfileCompleted = profile.getProfileImageType() != null;

        return new OnboardingStatusResponse(
                true,
                isBasicInfoCompleted,
                isSurveyCompleted,
                isSurveySkipped,
                isProfileCompleted
        );
    }
}
