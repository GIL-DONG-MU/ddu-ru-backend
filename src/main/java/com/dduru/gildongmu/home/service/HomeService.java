package com.dduru.gildongmu.home.service;

import com.dduru.gildongmu.common.enums.UserAccessStatus;
import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.home.HomeEndpoints;
import com.dduru.gildongmu.home.dto.response.HostResponse;
import com.dduru.gildongmu.home.dto.response.HomePopularDestinationItemResponse;
import com.dduru.gildongmu.home.dto.response.HomePopularDestinationResponse;
import com.dduru.gildongmu.home.dto.response.HomeResponse;
import com.dduru.gildongmu.home.dto.response.HomeSuperHostResponse;
import com.dduru.gildongmu.home.dto.response.MateRecommendationItemResponse;
import com.dduru.gildongmu.home.dto.response.MateRecommendationResponse;
import com.dduru.gildongmu.home.dto.response.SameAgeTripResponse;
import com.dduru.gildongmu.home.dto.response.SameDestinationTripResponse;
import com.dduru.gildongmu.home.dto.response.UpcomingTripResponse;
import com.dduru.gildongmu.onboarding.domain.UserOnboarding;
import com.dduru.gildongmu.onboarding.domain.enums.SurveyStatus;
import com.dduru.gildongmu.onboarding.repository.UserOnboardingRepository;
import com.dduru.gildongmu.post.domain.enums.PostStatus;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.profile.dto.response.ProfileImageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeService {

    // TODO: 이후 실제 데이터로 내보낼 예정
    private static final String DEFAULT_PROFILE_IMAGE_URL = "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fb/Karina_at_Love_Your_W_event_2025.jpg/500px-Karina_at_Love_Your_W_event_2025.jpg";
    private static final String DEFAULT_THUMBNAIL_URL = "https://img1.newsis.com/2022/07/05/NISI20220705_0001034620_web.jpg?rnd=20220705105652";

    private final TimeProvider timeProvider;
    private final UserOnboardingRepository userOnboardingRepository;

    @Transactional(readOnly = true)
    public HomeResponse retrieveHome(Long userId) {
        UserAccessStatus userAccessStatus = resolveUserAccessStatus(userId);
        return new HomeResponse(userAccessStatus, sections(userAccessStatus));
    }

    @Transactional(readOnly = true)
    public UpcomingTripResponse retrieveUpcomingTrip(Long userId) {
        LocalDate today = timeProvider.today();
        LocalDate upcomingStartDate = today.plusDays(12);
        LocalDate upcomingEndDate = upcomingStartDate.plusDays(3);

        return upcomingTrip(upcomingStartDate, upcomingEndDate, today);
    }

    @Transactional(readOnly = true)
    public MateRecommendationResponse retrieveMateRecommendations(Long userId) {
        LocalDate upcomingStartDate = timeProvider.today().plusDays(12);
        LocalDate upcomingEndDate = upcomingStartDate.plusDays(3);

        return mateRecommendation(upcomingStartDate, upcomingEndDate);
    }

    @Transactional(readOnly = true)
    public HomePopularDestinationResponse retrievePopularDestinations() {
        return new HomePopularDestinationResponse(
                timeProvider.now().withSecond(0).withNano(0),
                popularDestinationItems()
        );
    }

    @Transactional(readOnly = true)
    public List<HomeSuperHostResponse> retrieveSuperHosts(Long userId) {
        return superHosts(timeProvider.today().plusDays(12));
    }

    @Transactional(readOnly = true)
    public List<SameDestinationTripResponse> retrieveSameDestinationTrips(Long userId) {
        return sameDestinationTrips(timeProvider.today().plusDays(12));
    }

    @Transactional(readOnly = true)
    public List<SameAgeTripResponse> retrieveSameAgeTrips(Long userId) {
        return sameAgeTrips(timeProvider.today().plusDays(12));
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

    private static List<HomePopularDestinationItemResponse> popularDestinationItems() {
        return List.of(
                new HomePopularDestinationItemResponse(1, 1L, "제주도", DEFAULT_THUMBNAIL_URL, 14231, List.of("힐링", "드라이브", "바다")),
                new HomePopularDestinationItemResponse(2, 2L, "도쿄", DEFAULT_THUMBNAIL_URL, 14131, List.of("맛집", "쇼핑")),
                new HomePopularDestinationItemResponse(3, 3L, "부산", DEFAULT_THUMBNAIL_URL, 11842, List.of("바다")),
                new HomePopularDestinationItemResponse(4, 4L, "강릉", DEFAULT_THUMBNAIL_URL, 9864, List.of()),
                new HomePopularDestinationItemResponse(5, 5L, "여수", DEFAULT_THUMBNAIL_URL, 8421, List.of("야경", "먹방", "감성"))
        );
    }

    private static UpcomingTripResponse upcomingTrip(
            LocalDate startDate,
            LocalDate endDate,
            LocalDate today
    ) {
        return new UpcomingTripResponse(
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

    private static MateRecommendationResponse mateRecommendation(
            LocalDate startDate,
            LocalDate endDate
    ) {
        return new MateRecommendationResponse(
                true,
                3,
                List.of(
                        new MateRecommendationItemResponse(
                                5001L,
                                801L,
                                92,
                                "제주 서쪽 해안 카페 투어",
                                "제주 서귀포",
                                startDate,
                                endDate,
                                uploadedHost("여행자민지", 28, Gender.F),
                                3,
                                4,
                                "제주 서쪽 해안을 따라 사진 찍고 카페를 둘러볼 동행을 찾아요.",
                                List.of("#카페투어", "#사진", "#힐링")
                        ),
                        new MateRecommendationItemResponse(
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

    private static List<SameDestinationTripResponse> sameDestinationTrips(LocalDate baseStartDate) {
        return List.of(
                new SameDestinationTripResponse(
                        601L,
                        "제주 한라산 숲길 산책",
                        "제주도 한라산",
                        baseStartDate.plusDays(2),
                        baseStartDate.plusDays(4),
                        3,
                        4,
                        DEFAULT_THUMBNAIL_URL
                ),
                new SameDestinationTripResponse(
                        602L,
                        "우도 전기차 당일치기",
                        "제주 우도",
                        baseStartDate.plusDays(6),
                        baseStartDate.plusDays(6),
                        2,
                        4,
                        DEFAULT_THUMBNAIL_URL
                ),
                new SameDestinationTripResponse(
                        603L,
                        "서귀포 올레길 걷기",
                        "제주 서귀포",
                        baseStartDate.plusDays(9),
                        baseStartDate.plusDays(11),
                        1,
                        3,
                        DEFAULT_THUMBNAIL_URL
                )
        );
    }

    private static List<HomeSuperHostResponse> superHosts(LocalDate baseStartDate) {
        return List.of(
                new HomeSuperHostResponse(
                        501L,
                        PostStatus.OPEN,
                        "제주 동쪽 일출 투어",
                        "제주도 한라산",
                        baseStartDate,
                        baseStartDate.plusDays(3),
                        3,
                        4,
                        List.of("일출", "등산"),
                        uploadedHost("여행자민지", 28, Gender.F),
                        723,
                        DEFAULT_THUMBNAIL_URL,
                        false
                ),
                new HomeSuperHostResponse(
                        502L,
                        PostStatus.OPEN,
                        "부산 야경 맛집 산책",
                        "부산 광안리",
                        baseStartDate.plusDays(5),
                        baseStartDate.plusDays(7),
                        2,
                        5,
                        List.of("맛집", "야경"),
                        avatarHost("부산가이드", 34, Gender.M, 3L),
                        681,
                        DEFAULT_THUMBNAIL_URL,
                        false
                ),
                new HomeSuperHostResponse(
                        503L,
                        PostStatus.OPEN,
                        "강릉 바다 감성 여행",
                        "강원도 강릉",
                        baseStartDate.plusDays(8),
                        baseStartDate.plusDays(10),
                        4,
                        6,
                        List.of("바다", "사진"),
                        uploadedHost("바다수집가", 29, Gender.F),
                        598,
                        DEFAULT_THUMBNAIL_URL,
                        false
                ),
                new HomeSuperHostResponse(
                        504L,
                        PostStatus.OPEN,
                        "여수 밤바다 산책",
                        "전남 여수",
                        baseStartDate.plusDays(11),
                        baseStartDate.plusDays(13),
                        2,
                        4,
                        List.of("산책", "야경"),
                        avatarHost("여수러버", 32, Gender.U, 4L),
                        512,
                        DEFAULT_THUMBNAIL_URL,
                        false
                ),
                new HomeSuperHostResponse(
                        505L,
                        PostStatus.OPEN,
                        "전주 한옥마을 먹방",
                        "전주 한옥마을",
                        baseStartDate.plusDays(14),
                        baseStartDate.plusDays(15),
                        3,
                        5,
                        List.of("맛집", "한옥"),
                        uploadedHost("먹방메이트", 27, Gender.F),
                        476,
                        DEFAULT_THUMBNAIL_URL,
                        false
                )
        );
    }

    private static List<SameAgeTripResponse> sameAgeTrips(LocalDate baseStartDate) {
        return List.of(
                new SameAgeTripResponse(
                        701L,
                        "제주 로컬 맛집 탐방",
                        "제주도 한라산",
                        baseStartDate.plusDays(1),
                        3,
                        4,
                        DEFAULT_THUMBNAIL_URL
                ),
                new SameAgeTripResponse(
                        702L,
                        "부산 감천문화마을 산책",
                        "부산 감천문화마을",
                        baseStartDate.plusDays(4),
                        2,
                        5,
                        DEFAULT_THUMBNAIL_URL
                ),
                new SameAgeTripResponse(
                        703L,
                        "전주 한옥마을 먹방",
                        "전주 한옥마을",
                        baseStartDate.plusDays(7),
                        4,
                        6,
                        DEFAULT_THUMBNAIL_URL
                )
        );
    }

    private static HostResponse uploadedHost(String nickname, int age, Gender gender) {
        return new HostResponse(
                nickname,
                new ProfileImageInfo(
                        ProfileImageType.UPLOADED,
                        DEFAULT_PROFILE_IMAGE_URL,
                        null
                ),
                age,
                gender
        );
    }

    private static HostResponse avatarHost(String nickname, int age, Gender gender, Long bgColorId) {
        return new HostResponse(
                nickname,
                new ProfileImageInfo(
                        ProfileImageType.AVATAR,
                        DEFAULT_PROFILE_IMAGE_URL,
                        bgColorId
                ),
                age,
                gender
        );
    }
}
