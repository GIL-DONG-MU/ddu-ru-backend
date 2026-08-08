package com.dduru.gildongmu.home.service;

import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.home.dto.response.HomePopularDestinationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomePopularDestinationQueryService {

    private final TimeProvider timeProvider;

    @Transactional(readOnly = true)
    public HomePopularDestinationResponse retrieve() {
        return new HomePopularDestinationResponse(
                timeProvider.now().withSecond(0).withNano(0),
                popularDestinationItems()
        );
    }

    private static List<HomePopularDestinationResponse.Item> popularDestinationItems() {
        return List.of(
                new HomePopularDestinationResponse.Item(1, 1L, "제주도", HomeMockData.THUMBNAIL_URL, 14231, List.of("힐링", "드라이브", "바다")),
                new HomePopularDestinationResponse.Item(2, 2L, "도쿄", HomeMockData.THUMBNAIL_URL, 14131, List.of("맛집", "쇼핑")),
                new HomePopularDestinationResponse.Item(3, 3L, "부산", HomeMockData.THUMBNAIL_URL, 11842, List.of("바다")),
                new HomePopularDestinationResponse.Item(4, 4L, "강릉", HomeMockData.THUMBNAIL_URL, 9864, List.of()),
                new HomePopularDestinationResponse.Item(5, 5L, "여수", HomeMockData.THUMBNAIL_URL, 8421, List.of("야경", "먹방", "감성"))
        );
    }
}
