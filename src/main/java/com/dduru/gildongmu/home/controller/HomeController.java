package com.dduru.gildongmu.home.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.annotation.OptionalCurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.home.dto.response.HomePopularDestinationResponse;
import com.dduru.gildongmu.home.dto.response.HomeResponse;
import com.dduru.gildongmu.home.dto.response.HomeSuperHostResponse;
import com.dduru.gildongmu.home.dto.response.MateRecommendationResponse;
import com.dduru.gildongmu.home.dto.response.SameAgeTripResponse;
import com.dduru.gildongmu.home.dto.response.SameDestinationTripResponse;
import com.dduru.gildongmu.home.dto.response.UpcomingTripResponse;
import com.dduru.gildongmu.home.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HomeController implements HomeApiDocs {

    private final HomeService homeService;

    @Override
    @GetMapping("/api/v1/home")
    public ResponseEntity<ApiResult<HomeResponse>> retrieveHome(@OptionalCurrentUser Long userId) {
        HomeResponse response = homeService.retrieveHome(userId);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @GetMapping("/api/v1/home/upcoming-trip")
    public ResponseEntity<ApiResult<UpcomingTripResponse>> retrieveUpcomingTrip(
            @CurrentUser Long userId
    ) {
        UpcomingTripResponse response = homeService.retrieveUpcomingTrip(userId);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @GetMapping("/api/v1/home/popular-destinations")
    public ResponseEntity<ApiResult<HomePopularDestinationResponse>> retrievePopularDestinations() {
        HomePopularDestinationResponse response = homeService.retrievePopularDestinations();
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @GetMapping("/api/v1/home/mate-recommendations")
    public ResponseEntity<ApiResult<MateRecommendationResponse>> retrieveMateRecommendations(
            @CurrentUser Long userId
    ) {
        MateRecommendationResponse response = homeService.retrieveMateRecommendations(userId);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @GetMapping("/api/v1/home/super-hosts")
    public ResponseEntity<ApiResult<List<HomeSuperHostResponse>>> retrieveSuperHosts(
            @OptionalCurrentUser Long userId
    ) {
        List<HomeSuperHostResponse> response = homeService.retrieveSuperHosts(userId);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @GetMapping("/api/v1/home/same-destination-trips")
    public ResponseEntity<ApiResult<List<SameDestinationTripResponse>>> retrieveSameDestinationTrips(
            @CurrentUser Long userId
    ) {
        List<SameDestinationTripResponse> response = homeService.retrieveSameDestinationTrips(userId);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @GetMapping("/api/v1/home/same-age-trips")
    public ResponseEntity<ApiResult<List<SameAgeTripResponse>>> retrieveSameAgeTrips(
            @CurrentUser Long userId
    ) {
        List<SameAgeTripResponse> response = homeService.retrieveSameAgeTrips(userId);
        return ResponseEntity.ok(ApiResult.ok(response));
    }
}
