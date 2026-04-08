package com.dduru.gildongmu.participation.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.participation.dto.request.ParticipationRequest;
import com.dduru.gildongmu.participation.dto.request.ParticipationRetrieveRequest;
import com.dduru.gildongmu.participation.dto.response.ParticipationApproveResponse;
import com.dduru.gildongmu.participation.dto.response.ParticipationContactResponse;
import com.dduru.gildongmu.participation.dto.response.ParticipationCreateResponse;
import com.dduru.gildongmu.participation.dto.response.ParticipationRetrieveResponse;
import com.dduru.gildongmu.participation.service.ParticipationCommandService;
import com.dduru.gildongmu.participation.service.ParticipationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
    @PostMapping("/participations/{participationId}/contact")
    public ResponseEntity<ApiResult<ParticipationContactResponse>> contactParticipation(
            @CurrentUser Long userId,
            @PathVariable Long participationId
    ) {
        ParticipationContactResponse response = participationCommandService.contactParticipation(userId, participationId);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @PatchMapping("/participations/{participationId}/approve")
    public ResponseEntity<ApiResult<ParticipationApproveResponse>> approveParticipation(
            @CurrentUser Long userId,
            @PathVariable Long participationId
    ) {
        ParticipationApproveResponse response = participationCommandService.approveParticipation(userId, participationId);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @GetMapping("/participations")
    public ResponseEntity<ApiResult<List<ParticipationRetrieveResponse>>> getParticipants(
            @CurrentUser Long userId,
            @ParameterObject ParticipationRetrieveRequest request
    ) {
        List<ParticipationRetrieveResponse> participants = participationCommandService.retrieveAllParticipants(userId, request);
        return ResponseEntity.ok(ApiResult.ok(participants));
    }
}
