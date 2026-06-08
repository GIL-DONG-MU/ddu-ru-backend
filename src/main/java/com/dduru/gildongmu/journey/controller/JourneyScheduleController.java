package com.dduru.gildongmu.journey.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.journey.dto.request.JourneyScheduleCreateRequest;
import com.dduru.gildongmu.journey.dto.request.JourneyScheduleUpdateRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyScheduleListResponse;
import com.dduru.gildongmu.journey.service.JourneyScheduleService;
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
@RequestMapping("/api/v1/journeys/{journeyId}/schedules")
public class JourneyScheduleController implements JourneyScheduleApiDocs {

    private final JourneyScheduleService journeyScheduleService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResult<JourneyScheduleListResponse>> retrieveSchedules(
            @PathVariable Long journeyId,
            @CurrentUser Long userId
    ) {
        JourneyScheduleListResponse response = journeyScheduleService.retrieveSchedules(journeyId, userId);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @PostMapping
    public ResponseEntity<ApiResult<JourneyScheduleListResponse>> createSchedule(
            @PathVariable Long journeyId,
            @CurrentUser Long userId,
            @Valid @RequestBody JourneyScheduleCreateRequest request
    ) {
        JourneyScheduleListResponse response = journeyScheduleService.createSchedule(journeyId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.created(response));
    }

    @Override
    @PatchMapping("/{scheduleId}")
    public ResponseEntity<ApiResult<JourneyScheduleListResponse>> updateSchedule(
            @PathVariable Long journeyId,
            @PathVariable Long scheduleId,
            @CurrentUser Long userId,
            @Valid @RequestBody JourneyScheduleUpdateRequest request
    ) {
        JourneyScheduleListResponse response = journeyScheduleService.updateSchedule(journeyId, scheduleId, userId, request);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ApiResult<Void>> deleteSchedule(
            @PathVariable Long journeyId,
            @PathVariable Long scheduleId,
            @CurrentUser Long userId
    ) {
        journeyScheduleService.deleteSchedule(journeyId, scheduleId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResult.noContent());
    }
}
