package com.dduru.gildongmu.home.service;

import com.dduru.gildongmu.common.enums.UserAccessStatus;
import com.dduru.gildongmu.home.dto.response.HomeResponse;
import com.dduru.gildongmu.onboarding.domain.UserOnboarding;
import com.dduru.gildongmu.onboarding.domain.enums.SurveyStatus;
import com.dduru.gildongmu.onboarding.repository.UserOnboardingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeService {

    private static final String UPCOMING_TRIP_ENDPOINT = "/api/v1/home/upcoming-trip";
    private static final String POPULAR_DESTINATIONS_ENDPOINT = "/api/v1/home/popular-destinations";
    private static final String MATE_RECOMMENDATIONS_ENDPOINT = "/api/v1/home/mate-recommendations";
    private static final String SUPER_HOSTS_ENDPOINT = "/api/v1/home/super-hosts";
    private static final String SAME_DESTINATION_TRIPS_ENDPOINT = "/api/v1/home/same-destination-trips";
    private static final String SAME_AGE_TRIPS_ENDPOINT = "/api/v1/home/same-age-trips";

    private final UserOnboardingRepository userOnboardingRepository;

    @Transactional(readOnly = true)
    public HomeResponse retrieveHome(Long userId) {
        UserAccessStatus userAccessStatus = resolveUserAccessStatus(userId);
        return new HomeResponse(userAccessStatus, sections(userAccessStatus));
    }

    private UserAccessStatus resolveUserAccessStatus(Long userId) {
        if (userId == null) {
            return UserAccessStatus.GUEST;
        }

        return toUserAccessStatus(userOnboardingRepository.getByUserIdOrThrow(userId));
    }

    private static UserAccessStatus toUserAccessStatus(UserOnboarding onboarding) {
        if (onboarding.getSurveyStatus() == SurveyStatus.COMPLETED) {
            return UserAccessStatus.MEMBER_SURVEY_COMPLETED;
        }
        return UserAccessStatus.MEMBER_SURVEY_REQUIRED;
    }

    private static List<HomeResponse.HomeSectionResponse> sections(UserAccessStatus userAccessStatus) {
        return List.of(
                section(
                        HomeResponse.SectionKey.UPCOMING_TRIP,
                        UPCOMING_TRIP_ENDPOINT,
                        isMember(userAccessStatus),
                        HomeResponse.DisabledReason.LOGIN_REQUIRED
                ),
                section(
                        HomeResponse.SectionKey.POPULAR_DESTINATIONS,
                        POPULAR_DESTINATIONS_ENDPOINT,
                        true,
                        null
                ),
                section(
                        HomeResponse.SectionKey.MATE_RECOMMENDATIONS,
                        MATE_RECOMMENDATIONS_ENDPOINT,
                        isSurveyCompleted(userAccessStatus),
                        mateRecommendationDisabledReason(userAccessStatus)
                ),
                section(
                        HomeResponse.SectionKey.SUPER_HOSTS,
                        SUPER_HOSTS_ENDPOINT,
                        true,
                        null
                ),
                section(
                        HomeResponse.SectionKey.SAME_DESTINATION_TRIPS,
                        SAME_DESTINATION_TRIPS_ENDPOINT,
                        isMember(userAccessStatus),
                        HomeResponse.DisabledReason.LOGIN_REQUIRED
                ),
                section(
                        HomeResponse.SectionKey.SAME_AGE_TRIPS,
                        SAME_AGE_TRIPS_ENDPOINT,
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
        return new HomeResponse.HomeSectionResponse(
                key,
                enabled,
                endpoint,
                enabled ? null : disabledReason
        );
    }

    private static HomeResponse.DisabledReason mateRecommendationDisabledReason(UserAccessStatus userAccessStatus) {
        if (userAccessStatus == UserAccessStatus.GUEST) {
            return HomeResponse.DisabledReason.LOGIN_REQUIRED;
        }
        if (userAccessStatus == UserAccessStatus.MEMBER_SURVEY_REQUIRED) {
            return HomeResponse.DisabledReason.SURVEY_REQUIRED;
        }
        return null;
    }

    private static boolean isMember(UserAccessStatus userAccessStatus) {
        return userAccessStatus != UserAccessStatus.GUEST;
    }

    private static boolean isSurveyCompleted(UserAccessStatus userAccessStatus) {
        return userAccessStatus == UserAccessStatus.MEMBER_SURVEY_COMPLETED;
    }
}
