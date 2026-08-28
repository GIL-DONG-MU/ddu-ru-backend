package com.dduru.gildongmu.home.service;

import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.home.dto.response.SameAgeTripResponse;
import com.dduru.gildongmu.home.dto.response.SameDestinationTripResponse;
import com.dduru.gildongmu.home.dto.response.UpcomingTripResponse;
import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.journey.exception.JourneyNotFoundException;
import com.dduru.gildongmu.journey.repository.JourneyRepository;
import com.dduru.gildongmu.onboarding.service.OnboardingService;
import com.dduru.gildongmu.post.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeTripQueryService {

    private final TimeProvider timeProvider;
    private final OnboardingService onboardingService;
    private final JourneyRepository journeyRepository;

    @Transactional(readOnly = true)
    public UpcomingTripResponse retrieveUpcomingTrip(Long userId) {
        LocalDate today = timeProvider.today();
        Journey journey = journeyRepository
                .findCurrentAndUpcomingJourneys(userId, today, Pageable.ofSize(1))
                .stream()
                .findFirst()
                .orElseThrow(JourneyNotFoundException::new);
        Post post = journey.getPost();

        return new UpcomingTripResponse(
                journey.getId(),
                journey.getTitle(),
                Math.max(0, (int) ChronoUnit.DAYS.between(today, post.getStartDate())),
                post.getStartDate(),
                post.getEndDate(),
                post.getRecruitCount(),
                post.getRecruitCapacity(),
                0 // TODO: 여정 할 일 기능 연동 후 실제 미완료 개수를 조회한다.
        );
    }

    @Transactional(readOnly = true)
    public List<SameDestinationTripResponse> retrieveSameDestinationTrips(Long userId) {
        // TODO: 선호 여행지 설정 기능이 추가되면 온보딩 존재 확인 대신 선호 여행지 설정 여부를 검증한다.
        requireOnboarding(userId);
        LocalDate baseStartDate = timeProvider.today().plusDays(12);
        return List.of(
                new SameDestinationTripResponse(601L, "제주 한라산 숲길 산책", "제주도 한라산", baseStartDate.plusDays(2), baseStartDate.plusDays(4), 3, 4, HomeMockData.THUMBNAIL_URL),
                new SameDestinationTripResponse(602L, "우도 전기차 당일치기", "제주 우도", baseStartDate.plusDays(6), baseStartDate.plusDays(6), 2, 4, HomeMockData.THUMBNAIL_URL),
                new SameDestinationTripResponse(603L, "서귀포 올레길 걷기", "제주 서귀포", baseStartDate.plusDays(9), baseStartDate.plusDays(11), 1, 3, HomeMockData.THUMBNAIL_URL)
        );
    }

    @Transactional(readOnly = true)
    public List<SameAgeTripResponse> retrieveSameAgeTrips(Long userId) {
        requireOnboarding(userId);
        LocalDate baseStartDate = timeProvider.today().plusDays(12);
        return List.of(
                new SameAgeTripResponse(701L, "제주 로컬 맛집 탐방", "제주도 한라산", baseStartDate.plusDays(1), 3, 4, HomeMockData.THUMBNAIL_URL),
                new SameAgeTripResponse(702L, "부산 감천문화마을 산책", "부산 감천문화마을", baseStartDate.plusDays(4), 2, 5, HomeMockData.THUMBNAIL_URL),
                new SameAgeTripResponse(703L, "전주 한옥마을 먹방", "전주 한옥마을", baseStartDate.plusDays(7), 4, 6, HomeMockData.THUMBNAIL_URL)
        );
    }

    private void requireOnboarding(Long userId) {
        onboardingService.getStatus(userId);
    }
}
