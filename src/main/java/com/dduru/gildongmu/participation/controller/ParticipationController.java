package com.dduru.gildongmu.participation.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.participation.dto.request.ParticipationRequest;
import com.dduru.gildongmu.participation.dto.response.ParticipationCreateResponse;
import com.dduru.gildongmu.participation.dto.response.ParticipationResponse;
import com.dduru.gildongmu.participation.service.ParticipationCommandService;
import com.dduru.gildongmu.participation.service.ParticipationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ParticipationController implements ParticipationApiDocs {

    private final ParticipationService participationService;
    private final ParticipationCommandService participationCommandService;

    @Override
    @PostMapping("/posts/{postId}/participations")
    public ResponseEntity<ApiResult<ParticipationCreateResponse>> createParticipation(
            @CurrentUser Long userId,
            @PathVariable Long postId,
            @Valid @RequestBody ParticipationRequest request
    ) {
        ParticipationCreateResponse response = participationService.participate(userId, postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.created(response));
    }

    @Override
    @GetMapping("/posts/{postId}/participations")
    public ResponseEntity<ApiResult<List<ParticipationResponse>>> getPostParticipants(
            @CurrentUser Long userId,
            @PathVariable Long postId
    ) {
        List<ParticipationResponse> participants = participationCommandService.retrieveParticipantsByPost(userId, postId);
        return ResponseEntity.ok(ApiResult.ok(participants));
    }
}
