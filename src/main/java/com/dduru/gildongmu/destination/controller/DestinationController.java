package com.dduru.gildongmu.destination.controller;

import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.destination.dto.DestinationInfo;
import com.dduru.gildongmu.destination.service.DestinationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/destinations")
public class DestinationController implements DestinationApiDocs {

    private final DestinationService destinationService;

    @Override
    @GetMapping("/popular")
    public ResponseEntity<ApiResult<List<DestinationInfo>>> getPopularDestinations() {
        List<DestinationInfo> destinations = destinationService.getPopularDestinations();
        return ResponseEntity.ok(ApiResult.ok(destinations));
    }

    @Override
    @GetMapping
    public ResponseEntity<ApiResult<List<DestinationInfo>>> searchDestinations(
            @RequestParam(required = false) String keyword
    ) {
        List<DestinationInfo> destinations = destinationService.searchDestinations(keyword);
        return ResponseEntity.ok(ApiResult.ok(destinations));
    }
}
