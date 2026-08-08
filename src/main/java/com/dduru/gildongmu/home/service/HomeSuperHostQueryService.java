package com.dduru.gildongmu.home.service;

import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.home.dto.response.HomeHostResponse;
import com.dduru.gildongmu.home.dto.response.HomeSuperHostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeSuperHostQueryService {

    private final TimeProvider timeProvider;

    @Transactional(readOnly = true)
    public List<HomeSuperHostResponse> retrieve(Long userId) {
        LocalDate baseStartDate = timeProvider.today().plusDays(12);
        // TODO: userId로 hasLiked 여부 조회 (실제 데이터 연동 시)
        return List.of(
                superHost(501L, "제주 동쪽 일출 투어", "제주도 한라산", baseStartDate, baseStartDate.plusDays(3),
                        3, 4, List.of("일출", "등산"), uploadedHost("여행자민지", 28, HomeHostResponse.Gender.F), 723),
                superHost(502L, "부산 야경 맛집 산책", "부산 광안리", baseStartDate.plusDays(5), baseStartDate.plusDays(7),
                        2, 5, List.of("맛집", "야경"), avatarHost("부산가이드", 34, HomeHostResponse.Gender.M, 3L), 681),
                superHost(503L, "강릉 바다 감성 여행", "강원도 강릉", baseStartDate.plusDays(8), baseStartDate.plusDays(10),
                        4, 6, List.of("바다", "사진"), uploadedHost("바다수집가", 29, HomeHostResponse.Gender.F), 598),
                superHost(504L, "여수 밤바다 산책", "전남 여수", baseStartDate.plusDays(11), baseStartDate.plusDays(13),
                        2, 4, List.of("산책", "야경"), avatarHost("여수러버", 32, HomeHostResponse.Gender.U, 4L), 512),
                superHost(505L, "전주 한옥마을 먹방", "전주 한옥마을", baseStartDate.plusDays(14), baseStartDate.plusDays(15),
                        3, 5, List.of("맛집", "한옥"), uploadedHost("먹방메이트", 27, HomeHostResponse.Gender.F), 476)
        );
    }

    private static HomeSuperHostResponse superHost(
            Long postId,
            String title,
            String location,
            LocalDate startDate,
            LocalDate endDate,
            int currentMemberCount,
            int maxMemberCount,
            List<String> tags,
            HomeHostResponse host,
            int viewCount
    ) {
        return new HomeSuperHostResponse(
                postId,
                HomeSuperHostResponse.Status.OPEN,
                title,
                location,
                startDate,
                endDate,
                currentMemberCount,
                maxMemberCount,
                tags,
                host,
                viewCount,
                HomeMockData.THUMBNAIL_URL,
                false
        );
    }

    private static HomeHostResponse uploadedHost(String nickname, int age, HomeHostResponse.Gender gender) {
        return new HomeHostResponse(
                nickname,
                new HomeHostResponse.ProfileImage(
                        HomeHostResponse.ImageType.UPLOADED,
                        HomeMockData.PROFILE_IMAGE_URL,
                        null
                ),
                age,
                gender
        );
    }

    private static HomeHostResponse avatarHost(
            String nickname,
            int age,
            HomeHostResponse.Gender gender,
            Long bgColorId
    ) {
        return new HomeHostResponse(
                nickname,
                new HomeHostResponse.ProfileImage(
                        HomeHostResponse.ImageType.AVATAR,
                        HomeMockData.PROFILE_IMAGE_URL,
                        bgColorId
                ),
                age,
                gender
        );
    }
}
