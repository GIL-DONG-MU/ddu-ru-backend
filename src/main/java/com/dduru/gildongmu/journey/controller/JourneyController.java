package com.dduru.gildongmu.journey.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.journey.dto.request.JourneyMemberRoleUpdateRequest;
import com.dduru.gildongmu.journey.dto.request.JourneyUpdateRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyDetailResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyMainListResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyMemberInfo;
import com.dduru.gildongmu.journey.dto.response.JourneyMemberRoleResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyUpdateResponse;

import java.util.List;
import com.dduru.gildongmu.journey.service.JourneyQueryService;
import com.dduru.gildongmu.journey.service.JourneyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class JourneyController implements JourneyApiDocs {

    private final JourneyQueryService journeyQueryService;
    private final JourneyService journeyService;

    @Override
    @GetMapping("/users/me/journeys")
    public ResponseEntity<ApiResult<JourneyMainListResponse>> retrieveMyJourneys(
            @CurrentUser Long userId
    ) {
        JourneyMainListResponse response = journeyQueryService.retrieveMyJourneys(userId);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @GetMapping("/journeys/{journeyId}/members")
    public ResponseEntity<ApiResult<List<JourneyMemberInfo>>> retrieveJourneyMembers(
            @PathVariable Long journeyId,
            @CurrentUser Long userId
    ) {
        List<JourneyMemberInfo> members = journeyQueryService.retrieveJourneyMembers(journeyId, userId);
        return ResponseEntity.ok(ApiResult.ok(members));
    }

    @Override
    @GetMapping("/journeys/{journeyId}")
    public ResponseEntity<ApiResult<JourneyDetailResponse>> retrieveMyJourneyDetail(
            @PathVariable Long journeyId,
            @CurrentUser Long userId
    ) {
        JourneyDetailResponse response = journeyQueryService.retrieveMyJourneyDetail(journeyId, userId);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @PatchMapping("/journeys/{journeyId}")
    public ResponseEntity<ApiResult<JourneyUpdateResponse>> updateJourneyBasicInfo(
            @PathVariable Long journeyId,
            @CurrentUser Long userId,
            @Valid @RequestBody JourneyUpdateRequest request
    ) {
        JourneyUpdateResponse response = journeyService.updateBasicInfo(journeyId, userId, request);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @PutMapping("/journeys/{journeyId}/members/{memberUserId}/roles")
    public ResponseEntity<ApiResult<JourneyMemberRoleResponse>> updateMemberRole(
            @PathVariable Long journeyId,
            @PathVariable Long memberUserId,
            @CurrentUser Long userId,
            @Valid @RequestBody JourneyMemberRoleUpdateRequest request
    ) {
        JourneyMemberRoleResponse response = journeyService.updateMemberRole(journeyId, userId, memberUserId, request);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @DeleteMapping("/journeys/{journeyId}/members/{memberUserId}")
    public ResponseEntity<ApiResult<Void>> removeJourneyMember(
            @PathVariable Long journeyId,
            @PathVariable Long memberUserId,
            @CurrentUser Long userId
    ) {
        journeyService.removeMember(journeyId, userId, memberUserId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResult.noContent());
    }
}
