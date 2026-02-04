package com.dduru.gildongmu.participation.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.participation.dto.request.ParticipationRequest;
import com.dduru.gildongmu.participation.dto.response.ParticipationResponse;
import com.dduru.gildongmu.participation.service.ParticipationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ParticipationController implements ParticipationApiDocs {

    private final ParticipationService participationService;

    @Override
    @PostMapping("/posts/{postId}/participations")
    public ResponseEntity<ApiResult<ParticipationResponse>> createParticipation(
            @PathVariable Long postId,
            @CurrentUser Long userId,
            @Valid @RequestBody ParticipationRequest request
    ) {
        ParticipationResponse response = participationService.participate(postId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.created(response));
    }

    @Override
    @GetMapping("/posts/{postId}/participations")
    public ResponseEntity<ApiResult<List<ParticipationResponse>>> getPostParticipants(
            @PathVariable Long postId,
            @CurrentUser Long userId
    ) {
        List<ParticipationResponse> participants = participationService.getParticipationsByPost(postId, userId);
        return ResponseEntity.ok(ApiResult.ok(participants));
    }

    @Override
    @PatchMapping("/posts/{postId}/participations/{participationId}/approve")
    public ResponseEntity<ApiResult<Void>> approveParticipation(
            @PathVariable Long postId,
            @PathVariable Long participationId,
            @CurrentUser Long userId
    ) {
        participationService.approveParticipation(postId, participationId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResult.noContent());
    }

    @Override
    @PatchMapping("/posts/{postId}/participations/{participationId}/reject")
    public ResponseEntity<ApiResult<Void>> rejectParticipation(
            @PathVariable Long postId,
            @PathVariable Long participationId,
            @CurrentUser Long userId
    ) {
        participationService.rejectParticipation(postId, participationId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResult.noContent());
    }

    @Override
    @DeleteMapping("/posts/{postId}/participations/{participationId}")
    public ResponseEntity<ApiResult<Void>> cancelParticipation(
            @PathVariable Long postId,
            @PathVariable Long participationId,
            @CurrentUser Long userId
    ) {
        participationService.cancelParticipation(postId, participationId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResult.noContent());
    }
}
