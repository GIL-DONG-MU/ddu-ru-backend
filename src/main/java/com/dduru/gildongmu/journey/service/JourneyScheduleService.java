package com.dduru.gildongmu.journey.service;

import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.common.validation.S3ImageUrlValidator;
import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.journey.domain.JourneySchedule;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import com.dduru.gildongmu.journey.domain.enums.ScheduleCategory;
import com.dduru.gildongmu.journey.dto.request.JourneyScheduleCreateRequest;
import com.dduru.gildongmu.journey.dto.request.JourneyScheduleUpdateRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyScheduleListResponse;
import com.dduru.gildongmu.journey.exception.InvalidJourneyScheduleException;
import com.dduru.gildongmu.journey.exception.JourneyAccessDeniedException;
import com.dduru.gildongmu.journey.repository.JourneyMemberRepository;
import com.dduru.gildongmu.journey.repository.JourneyRepository;
import com.dduru.gildongmu.journey.repository.JourneyScheduleRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.s3.enums.S3ImageDirectory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class JourneyScheduleService {

    private final JourneyRepository journeyRepository;
    private final JourneyMemberRepository journeyMemberRepository;
    private final JourneyScheduleRepository journeyScheduleRepository;
    private final S3ImageUrlValidator s3ImageUrlValidator;
    private final TimeProvider timeProvider;

    @Transactional(readOnly = true)
    public JourneyScheduleListResponse retrieveSchedules(Long journeyId, Long userId) {
        Journey journey = getAccessibleJourneyWithPost(journeyId, userId);
        Post post = journey.getPost();
        List<JourneySchedule> schedules = journeyScheduleRepository.findActiveSchedulesByJourneyId(journeyId);
        return JourneyScheduleListResponse.of(journeyId, post.getStartDate(), post.getEndDate(), schedules);
    }

    public JourneyScheduleListResponse createSchedule(Long journeyId, Long userId, JourneyScheduleCreateRequest request) {
        Journey journey = getAccessibleJourneyWithPost(journeyId, userId);
        Post post = journey.getPost();

        int dayOffset = request.dayOffset();
        validateDayOffset(dayOffset, post.getStartDate(), post.getEndDate());

        JourneySchedule schedule = createJourneySchedule(journey, dayOffset, request);
        journeyScheduleRepository.saveAndFlush(schedule);

        log.info("나의 여정 일정 생성됨 - journeyId={}, scheduleId={}, userId={}",
                journeyId, schedule.getId(), userId);

        List<JourneySchedule> schedules = journeyScheduleRepository.findActiveSchedulesByJourneyId(journeyId);
        return JourneyScheduleListResponse.of(journeyId, post.getStartDate(), post.getEndDate(), schedules);
    }

    public JourneyScheduleListResponse updateSchedule(
            Long journeyId,
            Long scheduleId,
            Long userId,
            JourneyScheduleUpdateRequest request
    ) {
        Journey journey = getAccessibleJourneyWithPost(journeyId, userId);
        Post post = journey.getPost();

        JourneySchedule schedule = journeyScheduleRepository.getActiveByIdAndJourneyIdOrThrow(scheduleId, journeyId);

        String title = request.title();
        ScheduleCategory category = request.category();
        Integer dayOffset = request.dayOffset();
        LocalTime startTime = request.startTime();
        boolean applyStartTimePatch = startTime != null || Boolean.TRUE.equals(request.clearStartTime());
        LocalTime startValue = Boolean.TRUE.equals(request.clearStartTime()) ? null : startTime;
        LocalTime endTime = request.endTime();
        boolean applyEndTimePatch = endTime != null || Boolean.TRUE.equals(request.clearEndTime());
        LocalTime endValue = Boolean.TRUE.equals(request.clearEndTime()) ? null : endTime;
        String placeName = request.placeName();
        boolean applyMemoPatch = request.memo() != null;
        String memo = applyMemoPatch ? request.memo() : null;
        boolean applyImageUrlPatch = request.imageUrl() != null;
        String imageUrl = applyImageUrlPatch ? normalizeImageUrl(request.imageUrl()) : null;

        validateHasAnyPatch(title, category, dayOffset, applyStartTimePatch, applyEndTimePatch, placeName, applyMemoPatch, applyImageUrlPatch);
        validateEffectiveDayOffset(schedule, dayOffset, post);

        schedule.update(title, category, dayOffset, applyStartTimePatch, startValue, applyEndTimePatch, endValue, placeName, applyMemoPatch, memo, applyImageUrlPatch, imageUrl);
        journeyScheduleRepository.flush();

        log.info("나의 여정 일정 수정됨 - journeyId={}, scheduleId={}, userId={}",
                journeyId, scheduleId, userId);

        List<JourneySchedule> schedules = journeyScheduleRepository.findActiveSchedulesByJourneyId(journeyId);
        return JourneyScheduleListResponse.of(journeyId, post.getStartDate(), post.getEndDate(), schedules);
    }

    public void deleteSchedule(Long journeyId, Long scheduleId, Long userId) {
        getAccessibleJourneyWithPost(journeyId, userId);

        JourneySchedule schedule = journeyScheduleRepository.getActiveByIdAndJourneyIdOrThrow(scheduleId, journeyId);
        schedule.delete(userId, timeProvider.now());

        log.info("나의 여정 일정 삭제됨 - journeyId={}, scheduleId={}, userId={}",
                journeyId, scheduleId, userId);
    }

    private JourneySchedule createJourneySchedule(Journey journey, int dayOffset, JourneyScheduleCreateRequest request) {
        return JourneySchedule.create(
                journey,
                request.title(),
                request.category(),
                dayOffset,
                request.startTime(),
                request.endTime(),
                request.placeName(),
                request.memo(),
                normalizeImageUrl(request.imageUrl())
        );
    }

    private Journey getAccessibleJourneyWithPost(Long journeyId, Long userId) {
        Journey journey = journeyRepository.getByIdWithPostContextOrThrow(journeyId);
        boolean isActiveMember = journeyMemberRepository.existsByJourneyIdAndUserIdAndStatus(
                journeyId, userId, JourneyMemberStatus.ACTIVE
        );
        if (!isActiveMember) {
            throw new JourneyAccessDeniedException();
        }
        return journey;
    }

    private static void validateEffectiveDayOffset(JourneySchedule schedule, Integer dayOffset, Post post) {
        int effectiveDayOffset = dayOffset != null ? dayOffset : schedule.getDayOffset();
        validateDayOffset(effectiveDayOffset, post.getStartDate(), post.getEndDate());
    }

    private static void validateDayOffset(int dayOffset, LocalDate startDate, LocalDate endDate) {
        int totalDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (dayOffset < 0 || dayOffset >= totalDays) {
            throw InvalidJourneyScheduleException.invalidScheduleDate();
        }
    }

    private static void validateHasAnyPatch(
            String title,
            ScheduleCategory category,
            Integer dayOffset,
            boolean applyStartTimePatch,
            boolean applyEndTimePatch,
            String placeName,
            boolean applyMemoPatch,
            boolean applyImageUrlPatch
    ) {
        if (title == null && category == null && dayOffset == null
                && !applyStartTimePatch && !applyEndTimePatch && placeName == null
                && !applyMemoPatch && !applyImageUrlPatch) {
            throw InvalidJourneyScheduleException.emptyPatch();
        }
    }

    private String normalizeImageUrl(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            return null;
        }
        return s3ImageUrlValidator.validateAndNormalize(imageUrl, S3ImageDirectory.JOURNEY_SCHEDULES);
    }

}
