package com.dduru.gildongmu.participation.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.participation.dto.ParticipationRequest;
import com.dduru.gildongmu.participation.dto.ParticipationResponse;
import com.dduru.gildongmu.participation.service.ParticipationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ParticipationController implements ParticipationApiDocs {

    private final ParticipationService participationService;

    @Override
    @PostMapping("/posts/{postId}/participations")
    public ResponseEntity<ParticipationResponse> createParticipation(
            @PathVariable Long postId,
            @CurrentUser Long userId,
            @Valid @RequestBody ParticipationRequest request
    ) {
        ParticipationResponse response = participationService.participate(postId, userId, request);
        return ResponseEntity
                .created(URI.create("/api/v1/participations/" + response.id())).body(response);
    }

    @Override
    @GetMapping("/posts/{postId}/participations")
    public ResponseEntity<List<ParticipationResponse>> getPostParticipants(
            @PathVariable Long postId,
            @CurrentUser Long userId
    ) {
        List<ParticipationResponse> participants = participationService.getParticipationsByPost(postId, userId);
        return ResponseEntity.ok(participants);
    }

    @Override
    @PatchMapping("/posts/{postId}/participations/{participationId}/approve")
    public ResponseEntity<Void> approveParticipation(
            @PathVariable Long postId,
            @PathVariable Long participationId,
            @CurrentUser Long userId
    ) {
        participationService.approveParticipation(postId, participationId, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/posts/{postId}/participations/{participationId}/reject")
    public ResponseEntity<Void> rejectParticipation(
            @PathVariable Long postId,
            @PathVariable Long participationId,
            @CurrentUser Long userId
    ) {
        participationService.rejectParticipation(postId, participationId, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/posts/{postId}/participations/{participationId}")
    public ResponseEntity<Void> cancelParticipation(
            @PathVariable Long postId,
            @PathVariable Long participationId,
            @CurrentUser Long userId
    ) {
        participationService.cancelParticipation(postId, participationId, userId);
        return ResponseEntity.noContent().build();
    }
}
