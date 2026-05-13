package com.dduru.gildongmu.journey.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.journey.dto.request.JourneyPostCommentCreateRequest;
import com.dduru.gildongmu.journey.dto.request.JourneyPostCommentUpdateRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyPostCommentListResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyPostCommentResponse;
import com.dduru.gildongmu.journey.service.JourneyPostCommentService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/journeys/{journeyId}/posts/{journeyPostId}/comments")
public class JourneyPostCommentController implements JourneyPostCommentApiDocs {

    private final JourneyPostCommentService journeyPostCommentService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResult<JourneyPostCommentListResponse>> retrieveComments(
            @PathVariable Long journeyId,
            @PathVariable Long journeyPostId,
            @RequestParam(required = false) Integer limit,
            @CurrentUser Long userId
    ) {
        JourneyPostCommentListResponse response = journeyPostCommentService.retrieveComments(
                journeyId,
                journeyPostId,
                userId,
                limit
        );
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @PostMapping
    public ResponseEntity<ApiResult<JourneyPostCommentResponse>> createComment(
            @PathVariable Long journeyId,
            @PathVariable Long journeyPostId,
            @CurrentUser Long userId,
            @Valid @RequestBody JourneyPostCommentCreateRequest request
    ) {
        JourneyPostCommentResponse response = journeyPostCommentService.createComment(
                journeyId,
                journeyPostId,
                userId,
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.created(response));
    }

    @Override
    @PatchMapping("/{commentId}")
    public ResponseEntity<ApiResult<JourneyPostCommentResponse>> updateComment(
            @PathVariable Long journeyId,
            @PathVariable Long journeyPostId,
            @PathVariable Long commentId,
            @CurrentUser Long userId,
            @Valid @RequestBody JourneyPostCommentUpdateRequest request
    ) {
        JourneyPostCommentResponse response = journeyPostCommentService.updateComment(
                journeyId,
                journeyPostId,
                commentId,
                userId,
                request
        );
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResult<Void>> deleteComment(
            @PathVariable Long journeyId,
            @PathVariable Long journeyPostId,
            @PathVariable Long commentId,
            @CurrentUser Long userId
    ) {
        journeyPostCommentService.deleteComment(journeyId, journeyPostId, commentId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResult.noContent());
    }
}
