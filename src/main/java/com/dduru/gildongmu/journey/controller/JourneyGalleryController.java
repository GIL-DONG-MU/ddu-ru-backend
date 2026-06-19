package com.dduru.gildongmu.journey.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.journey.dto.request.JourneyGalleryRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyGalleryResponse;
import com.dduru.gildongmu.journey.service.JourneyGalleryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/journeys/{journeyId}/gallery")
public class JourneyGalleryController implements JourneyGalleryApiDocs {

    private final JourneyGalleryService journeyGalleryService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResult<JourneyGalleryResponse>> retrieveGallery(
            @PathVariable Long journeyId,
            @CurrentUser Long userId,
            @Valid JourneyGalleryRequest request
    ) {
        JourneyGalleryResponse response = journeyGalleryService.retrieveGallery(journeyId, userId, request);
        return ResponseEntity.ok(ApiResult.ok(response));
    }
}
