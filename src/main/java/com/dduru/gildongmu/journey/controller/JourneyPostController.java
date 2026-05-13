package com.dduru.gildongmu.journey.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.journey.dto.request.JourneyPostCreateRequest;
import com.dduru.gildongmu.journey.dto.request.JourneyPostListRequest;
import com.dduru.gildongmu.journey.dto.request.JourneyPostUpdateRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyPostListResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyPostResponse;
import com.dduru.gildongmu.journey.service.JourneyPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/journeys/{journeyId}/posts")
public class JourneyPostController implements JourneyPostApiDocs {

    private final JourneyPostService journeyPostService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResult<JourneyPostListResponse>> retrievePosts(
            @PathVariable Long journeyId,
            @CurrentUser Long userId,
            @Valid JourneyPostListRequest request
    ) {
        JourneyPostListResponse response = journeyPostService.retrievePosts(journeyId, userId, request);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @GetMapping("/{journeyPostId}")
    public ResponseEntity<ApiResult<JourneyPostResponse>> retrievePost(
            @PathVariable Long journeyId,
            @PathVariable Long journeyPostId,
            @CurrentUser Long userId
    ) {
        JourneyPostResponse response = journeyPostService.retrievePost(journeyId, journeyPostId, userId);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @PostMapping
    public ResponseEntity<ApiResult<JourneyPostResponse>> createPost(
            @PathVariable Long journeyId,
            @CurrentUser Long userId,
            @Valid @RequestBody JourneyPostCreateRequest request
    ) {
        JourneyPostResponse response = journeyPostService.createPost(journeyId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.created(response));
    }

    @Override
    @PatchMapping("/{journeyPostId}")
    public ResponseEntity<ApiResult<JourneyPostResponse>> updatePost(
            @PathVariable Long journeyId,
            @PathVariable Long journeyPostId,
            @CurrentUser Long userId,
            @Valid @RequestBody JourneyPostUpdateRequest request
    ) {
        JourneyPostResponse response = journeyPostService.updatePost(journeyId, journeyPostId, userId, request);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @DeleteMapping("/{journeyPostId}")
    public ResponseEntity<ApiResult<Void>> deletePost(
            @PathVariable Long journeyId,
            @PathVariable Long journeyPostId,
            @CurrentUser Long userId
    ) {
        journeyPostService.deletePost(journeyId, journeyPostId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResult.noContent());
    }
}
