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
public class ParticipationController {

    private final ParticipationService participationService;

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

    @GetMapping("/posts/{postId}/participations")
    public ResponseEntity<List<ParticipationResponse>> getPostParticipants(
            @PathVariable Long postId,
            @CurrentUser Long userId
    ) {
        List<ParticipationResponse> participants = participationService.getParticipationsByPost(postId, userId);
        return ResponseEntity.ok(participants);
    }

    @PatchMapping("/participations/{id}/approve")
    public ResponseEntity<Void> approveParticipation(
            @PathVariable Long id,
            @CurrentUser Long userId
    ) {
        participationService.approveParticipation(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/participations/{id}/reject")
    public ResponseEntity<Void> rejectParticipation(
            @PathVariable Long id,
            @CurrentUser Long userId
    ) {
        participationService.rejectParticipation(id, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/participations/{id}")
    public ResponseEntity<Void> cancelParticipation(
            @PathVariable Long id,
            @CurrentUser Long userId
    ) {
        participationService.cancelParticipation(id, userId);
        return ResponseEntity.noContent().build();
    }
}
