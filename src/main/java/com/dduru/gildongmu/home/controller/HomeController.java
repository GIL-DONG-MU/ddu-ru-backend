package com.dduru.gildongmu.home.controller;

import com.dduru.gildongmu.common.annotation.OptionalCurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.home.dto.response.HomeResponse;
import com.dduru.gildongmu.home.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
