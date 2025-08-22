package com.dduru.gildongmu.participation.controller;

import com.dduru.gildongmu.participation.dto.ParticipationRequest;
import com.dduru.gildongmu.participation.dto.ParticipationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Participations", description = "동행 게시글 참여 관리 API")
@SecurityRequirement(name = "JWT")
public interface ParticipationApiDocs {

    @Operation(summary = "참여 신청", description = "게시글에 참여 신청을 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "참여 신청 성공"),
    })
    ResponseEntity<ParticipationResponse> createParticipation(
            @Parameter(description = "게시글 ID") Long postId,
            @Parameter(hidden = true) Long userId,
            @Valid ParticipationRequest request
    );

    @Operation(summary = "게시글 참여자 목록 조회", description = "게시글에 참여한 사용자 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "참여자 목록 조회 성공"),
    })
    ResponseEntity<List<ParticipationResponse>> getPostParticipants(
            @Parameter(description = "게시글 ID") Long postId,
            @Parameter(hidden = true) Long userId
    );

    @Operation(summary = "참여 신청 승인", description = "게시글 참여 신청을 승인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "참여 신청 승인 성공"),
    })
    ResponseEntity<Void> approveParticipation(
            @Parameter(description = "동행 참여 신청 ID") Long id,
            @Parameter(hidden = true) Long userId
    );

    @Operation(summary = "참여 신청 거절", description = "게시글 참여 신청을 거절합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "참여 신청 거절 성공"),
    })
    ResponseEntity<Void> rejectParticipation(
            @Parameter(description = "동행 참여 신청 ID") Long id,
            @Parameter(hidden = true) Long userId
    );

    @Operation(summary = "참여 취소", description = "게시글 참여를 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "참여 취소 성공"),
    })
    ResponseEntity<Void> cancelParticipation(
            @Parameter(description = "동행 참여 신청 ID") Long id,
            @Parameter(hidden = true) Long userId
    );
}
