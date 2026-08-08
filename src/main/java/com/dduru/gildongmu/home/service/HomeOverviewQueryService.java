package com.dduru.gildongmu.home.service;

import com.dduru.gildongmu.home.controller.HomeEndpoints;
import com.dduru.gildongmu.home.dto.response.HomeResponse;
import com.dduru.gildongmu.home.enums.UserAccessStatus;
import com.dduru.gildongmu.onboarding.domain.enums.SurveyStatus;
import com.dduru.gildongmu.onboarding.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeOverviewQueryService {

    private final OnboardingService onboardingService;

    @Transactional(readOnly = true)
    public HomeResponse retrieve(Long userId) {
        UserAccessStatus userAccessStatus = resolveUserAccessStatus(userId);
        return new HomeResponse(userAccessStatus, sections(userAccessStatus));
    }

    private UserAccessStatus resolveUserAccessStatus(Long userId) {
        if (userId == null) {
            return UserAccessStatus.GUEST;
        }
        SurveyStatus surveyStatus = onboardingService.getStatus(userId).surveyStatus();
        if (surveyStatus == SurveyStatus.COMPLETED) {
            return UserAccessStatus.MEMBER_SURVEY_COMPLETED;
        }
        return UserAccessStatus.MEMBER_SURVEY_REQUIRED;
    }

    private static List<HomeResponse.HomeSectionResponse> sections(UserAccessStatus userAccessStatus) {
        return List.of(
                section(
                        HomeResponse.SectionKey.UPCOMING_TRIP,
                        HomeEndpoints.UPCOMING_TRIP,
                        isMember(userAccessStatus),
                        HomeResponse.DisabledReason.LOGIN_REQUIRED
                ),
                section(
                        HomeResponse.SectionKey.POPULAR_DESTINATIONS,
                        HomeEndpoints.POPULAR_DESTINATIONS,
                        true,
                        null
                ),
                section(
                        HomeResponse.SectionKey.MATE_RECOMMENDATIONS,
                        HomeEndpoints.MATE_RECOMMENDATIONS,
                        isSurveyCompleted(userAccessStatus),
                        mateRecommendationDisabledReason(userAccessStatus)
                ),
                section(
                        HomeResponse.SectionKey.SUPER_HOSTS,
                        HomeEndpoints.SUPER_HOSTS,
                        true,
                        null
                ),
                section(
                        HomeResponse.SectionKey.SAME_DESTINATION_TRIPS,
                        HomeEndpoints.SAME_DESTINATION_TRIPS,
                        isMember(userAccessStatus),
                        HomeResponse.DisabledReason.LOGIN_REQUIRED
                ),
                section(
                        HomeResponse.SectionKey.SAME_AGE_TRIPS,
                        HomeEndpoints.SAME_AGE_TRIPS,
                        isMember(userAccessStatus),
                        HomeResponse.DisabledReason.LOGIN_REQUIRED
                )
        );
    }

    private static HomeResponse.HomeSectionResponse section(
            HomeResponse.SectionKey key,
            String endpoint,
            boolean enabled,
            HomeResponse.DisabledReason disabledReason
    ) {
        return new HomeResponse.HomeSectionResponse(key, enabled, endpoint, enabled ? null : disabledReason);
    }

    private static HomeResponse.DisabledReason mateRecommendationDisabledReason(UserAccessStatus userAccessStatus) {
        return switch (userAccessStatus) {
            case GUEST -> HomeResponse.DisabledReason.LOGIN_REQUIRED;
            case MEMBER_SURVEY_REQUIRED -> HomeResponse.DisabledReason.SURVEY_REQUIRED;
            case MEMBER_SURVEY_COMPLETED -> null;
        };
    }

    private static boolean isMember(UserAccessStatus userAccessStatus) {
        return userAccessStatus != UserAccessStatus.GUEST;
    }

    private static boolean isSurveyCompleted(UserAccessStatus userAccessStatus) {
        return userAccessStatus == UserAccessStatus.MEMBER_SURVEY_COMPLETED;
    }
}
