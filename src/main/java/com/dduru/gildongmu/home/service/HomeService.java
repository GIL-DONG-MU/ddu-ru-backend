package com.dduru.gildongmu.home.service;

import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.home.dto.response.HomeResponse;
import com.dduru.gildongmu.onboarding.domain.UserOnboarding;
import com.dduru.gildongmu.onboarding.domain.enums.SurveyStatus;
import com.dduru.gildongmu.onboarding.repository.UserOnboardingRepository;
import com.dduru.gildongmu.post.domain.enums.PostStatus;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeService {

    private static final String DEFAULT_PROFILE_IMAGE_URL = "https://example.com/profile.jpg";
    private static final String DEFAULT_THUMBNAIL_URL = "https://example.com/trip-thumbnail.jpg";

    private final TimeProvider timeProvider;
    private final UserOnboardingRepository userOnboardingRepository;

    @Transactional(readOnly = true)
    public HomeResponse retrieveHome(Long userId) {
        HomeResponse.ViewerStatus viewerStatus = resolveViewerStatus(userId);
        LocalDate today = timeProvider.today();
        LocalDateTime updateDateTime = timeProvider.now().withSecond(0).withNano(0);

        LocalDate upcomingStartDate = today.plusDays(12);
        LocalDate upcomingEndDate = upcomingStartDate.plusDays(3);

        return new HomeResponse(
                viewerStatus,
                isMember(viewerStatus) ? upcomingTrip(upcomingStartDate, upcomingEndDate, today) : null,
                popularDestinations(updateDateTime),
                isSurveyCompleted(viewerStatus) ? mateRecommendation(upcomingStartDate, upcomingEndDate) : null,
                superHosts(upcomingStartDate),
                sameDestinationTrips(upcomingStartDate),
                isMember(viewerStatus) ? sameAgeTrips(upcomingStartDate) : null
        );
    }

    private HomeResponse.ViewerStatus resolveViewerStatus(Long userId) {
        if (userId == null) {
            return HomeResponse.ViewerStatus.GUEST;
        }

        return userOnboardingRepository.findByUser_Id(userId)
                .map(this::toViewerStatus)
                .orElse(HomeResponse.ViewerStatus.MEMBER_SURVEY_REQUIRED);
    }

    private HomeResponse.ViewerStatus toViewerStatus(UserOnboarding onboarding) {
        if (onboarding.getSurveyStatus() == SurveyStatus.COMPLETED) {
            return HomeResponse.ViewerStatus.MEMBER_SURVEY_COMPLETED;
        }
        return HomeResponse.ViewerStatus.MEMBER_SURVEY_REQUIRED;
    }

    private boolean isMember(HomeResponse.ViewerStatus viewerStatus) {
        return viewerStatus != HomeResponse.ViewerStatus.GUEST;
    }

    private boolean isSurveyCompleted(HomeResponse.ViewerStatus viewerStatus) {
        return viewerStatus == HomeResponse.ViewerStatus.MEMBER_SURVEY_COMPLETED;
    }

    private HomeResponse.UpcomingTripResponse upcomingTrip(
            LocalDate startDate,
            LocalDate endDate,
            LocalDate today
    ) {
        return new HomeResponse.UpcomingTripResponse(
                102L,
                "제주도 힐링 여행",
                (int) ChronoUnit.DAYS.between(today, startDate),
                startDate,
                endDate,
                3,
                4,
                1
        );
    }

    private HomeResponse.PopularDestinationsResponse popularDestinations(LocalDateTime updateDateTime) {
        return new HomeResponse.PopularDestinationsResponse(
                updateDateTime,
                List.of(
                        new HomeResponse.PopularDestinationResponse(1, "제주도", 14231),
                        new HomeResponse.PopularDestinationResponse(2, "부산", 11842),
                        new HomeResponse.PopularDestinationResponse(3, "강릉", 9864),
                        new HomeResponse.PopularDestinationResponse(4, "여수", 8421),
                        new HomeResponse.PopularDestinationResponse(5, "전주", 7732),
                        new HomeResponse.PopularDestinationResponse(6, "속초", 6950),
                        new HomeResponse.PopularDestinationResponse(7, "경주", 6427)
                )
        );
    }

    private HomeResponse.MateRecommendationResponse mateRecommendation(
            LocalDate startDate,
            LocalDate endDate
    ) {
        HomeResponse.HostResponse host = uploadedHost("여행자민지", 28, Gender.F);

        return new HomeResponse.MateRecommendationResponse(
                true,
                3,
                List.of(
                        new HomeResponse.MateRecommendationItemResponse(
                                5001L,
                                801L,
                                92,
                                "제주 서쪽 해안 카페 투어",
                                "제주 서귀포",
                                startDate,
                                endDate,
                                host,
                                3,
                                4,
                                "제주 서쪽 해안을 따라 사진 찍고 카페를 둘러볼 동행을 찾아요.",
                                List.of("#카페투어", "#사진", "#힐링")
                        ),
                        new HomeResponse.MateRecommendationItemResponse(
                                5002L,
                                802L,
                                87,
                                "한라산 초보 등반 메이트",
                                "제주 한라산",
                                startDate.plusDays(1),
                                endDate.plusDays(1),
                                avatarHost("오름러버", 31, Gender.M, 2L),
                                2,
                                4,
                                "무리하지 않고 천천히 한라산을 오를 동행을 모집합니다.",
                                List.of("#등산", "#자연", "#느긋한")
                        )
                )
        );
    }

    private List<HomeResponse.SuperHostResponse> superHosts(LocalDate baseStartDate) {
        return List.of(
                new HomeResponse.SuperHostResponse(
                        501L,
                        PostStatus.OPEN,
                        "제주 동쪽 일출 투어",
                        "제주도 성산일출봉",
                        baseStartDate,
                        baseStartDate.plusDays(3),
                        3,
                        4,
                        List.of("힐링", "자연", "느긋한"),
                        uploadedHost("해돋이호스트", 28, Gender.F),
                        723,
                        "https://example.com/sunrise.jpg"
                ),
                new HomeResponse.SuperHostResponse(
                        502L,
                        PostStatus.OPEN,
                        "부산 야경 맛집 산책",
                        "부산 광안리",
                        baseStartDate.plusDays(5),
                        baseStartDate.plusDays(7),
                        2,
                        5,
                        List.of("맛집", "야경", "산책"),
                        avatarHost("부산가이드", 34, Gender.M, 3L),
                        681,
                        "https://example.com/busan-night.jpg"
                ),
                new HomeResponse.SuperHostResponse(
                        503L,
                        PostStatus.OPEN,
                        "강릉 바다 감성 여행",
                        "강원도 강릉",
                        baseStartDate.plusDays(8),
                        baseStartDate.plusDays(10),
                        4,
                        6,
                        List.of("바다", "사진", "카페"),
                        uploadedHost("바다수집가", 29, Gender.F),
                        598,
                        "https://example.com/gangneung-sea.jpg"
                )
        );
    }

    private List<HomeResponse.SameDestinationTripResponse> sameDestinationTrips(LocalDate baseStartDate) {
        return List.of(
                new HomeResponse.SameDestinationTripResponse(
                        601L,
                        "제주 한라산 숲길 산책",
                        "제주도 한라산",
                        baseStartDate.plusDays(2),
                        baseStartDate.plusDays(4),
                        3,
                        4,
                        DEFAULT_THUMBNAIL_URL
                ),
                new HomeResponse.SameDestinationTripResponse(
                        602L,
                        "우도 전기차 당일치기",
                        "제주 우도",
                        baseStartDate.plusDays(6),
                        baseStartDate.plusDays(6),
                        2,
                        4,
                        "https://example.com/udo.jpg"
                ),
                new HomeResponse.SameDestinationTripResponse(
                        603L,
                        "서귀포 올레길 걷기",
                        "제주 서귀포",
                        baseStartDate.plusDays(9),
                        baseStartDate.plusDays(11),
                        1,
                        3,
                        "https://example.com/olle.jpg"
                )
        );
    }

    private List<HomeResponse.SameAgeTripResponse> sameAgeTrips(LocalDate baseStartDate) {
        return List.of(
                new HomeResponse.SameAgeTripResponse(
                        701L,
                        "제주 로컬 맛집 탐방",
                        "제주도 한라산",
                        baseStartDate.plusDays(1),
                        3,
                        4,
                        DEFAULT_THUMBNAIL_URL
                ),
                new HomeResponse.SameAgeTripResponse(
                        702L,
                        "부산 감천문화마을 산책",
                        "부산 감천문화마을",
                        baseStartDate.plusDays(4),
                        2,
                        5,
                        "https://example.com/gamcheon.jpg"
                ),
                new HomeResponse.SameAgeTripResponse(
                        703L,
                        "전주 한옥마을 먹방",
                        "전주 한옥마을",
                        baseStartDate.plusDays(7),
                        4,
                        6,
                        "https://example.com/jeonju.jpg"
                )
        );
    }

    private HomeResponse.HostResponse uploadedHost(String nickname, int age, Gender gender) {
        return new HomeResponse.HostResponse(
                nickname,
                new HomeResponse.ProfileImageInfoResponse(
                        ProfileImageType.UPLOADED,
                        DEFAULT_PROFILE_IMAGE_URL,
                        null
                ),
                age,
                gender
        );
    }

    private HomeResponse.HostResponse avatarHost(String nickname, int age, Gender gender, Long bgColorId) {
        return new HomeResponse.HostResponse(
                nickname,
                new HomeResponse.ProfileImageInfoResponse(
                        ProfileImageType.AVATAR,
                        "https://example.com/avatar.png",
                        bgColorId
                ),
                age,
                gender
        );
    }
}
