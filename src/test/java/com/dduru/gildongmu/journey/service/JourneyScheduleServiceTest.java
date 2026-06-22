package com.dduru.gildongmu.journey.service;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.journey.domain.JourneySchedule;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import com.dduru.gildongmu.journey.domain.enums.ScheduleCategory;
import com.dduru.gildongmu.journey.dto.request.JourneyScheduleCreateRequest;
import com.dduru.gildongmu.journey.dto.request.JourneyScheduleUpdateRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyScheduleListResponse;
import com.dduru.gildongmu.journey.exception.InvalidJourneyScheduleException;
import com.dduru.gildongmu.journey.exception.JourneyAccessDeniedException;
import com.dduru.gildongmu.journey.exception.JourneyScheduleNotFoundException;
import com.dduru.gildongmu.journey.repository.JourneyMemberRepository;
import com.dduru.gildongmu.journey.repository.JourneyRepository;
import com.dduru.gildongmu.journey.repository.JourneyScheduleRepository;
import org.springframework.context.ApplicationEventPublisher;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JourneyScheduleService 테스트")
class JourneyScheduleServiceTest {
    private static final LocalDate START_DATE = LocalDate.of(2026, 3, 10);
    private static final LocalDate END_DATE = LocalDate.of(2026, 3, 12);
    private static final LocalTime START_TIME = LocalTime.of(9, 0);
    private static final LocalTime END_TIME = LocalTime.of(11, 0);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 10, 12, 0);

    @Mock
    private JourneyRepository journeyRepository;
    @Mock
    private JourneyMemberRepository journeyMemberRepository;
    @Mock
    private JourneyScheduleRepository journeyScheduleRepository;
    @Mock
    private TimeProvider timeProvider;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private JourneyScheduleService journeyScheduleService;

    @BeforeEach
    void setUp() {
        journeyScheduleService = new JourneyScheduleService(
                journeyRepository,
                journeyMemberRepository,
                journeyScheduleRepository,
                timeProvider,
                eventPublisher
        );
    }

    // ─────────────────────────────────────────────────────────────
    // 일정 목록 조회
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("일정 목록 조회")
    class GetSchedules {

        @Test
        @DisplayName("active member는 여행 기간 전체의 Day 구조로 일정을 조회할 수 있다")
        void activeMemberCanGetSchedules() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);
            JourneySchedule schedule = createSchedule(101L, journey, 0);

            givenActiveMember(journeyId, userId, journey);
            when(journeyScheduleRepository.findActiveSchedulesByJourneyId(journeyId))
                    .thenReturn(List.of(schedule));

            JourneyScheduleListResponse response = journeyScheduleService.retrieveSchedules(journeyId, userId);

            assertThat(response.journeyId()).isEqualTo(journeyId);
            assertThat(response.startDate()).isEqualTo(START_DATE);
            assertThat(response.endDate()).isEqualTo(END_DATE);
            assertThat(response.days()).hasSize(3);
            assertThat(response.days().get(0).day()).isEqualTo(1);
            assertThat(response.days().get(0).date()).isEqualTo(START_DATE);
            assertThat(response.days().get(0).schedules()).hasSize(1);
            assertThat(response.days().get(0).schedules().get(0).scheduleId()).isEqualTo(101L);
        }

        @Test
        @DisplayName("일정이 없는 날짜도 빈 schedules 리스트와 함께 Day 구조로 반환된다")
        void emptyDaysAreIncluded() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);

            givenActiveMember(journeyId, userId, journey);
            when(journeyScheduleRepository.findActiveSchedulesByJourneyId(journeyId))
                    .thenReturn(List.of());

            JourneyScheduleListResponse response = journeyScheduleService.retrieveSchedules(journeyId, userId);

            assertThat(response.days()).hasSize(3);
            assertThat(response.days()).allMatch(day -> day.schedules().isEmpty());
            assertThat(response.days().get(0).day()).isEqualTo(1);
            assertThat(response.days().get(1).day()).isEqualTo(2);
            assertThat(response.days().get(2).day()).isEqualTo(3);
        }

        @Test
        @DisplayName("같은 날짜의 일정 여러 개가 올바른 Day에 묶여 반환된다")
        void schedulesGroupedByDate() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);
            JourneySchedule morning = createSchedule(101L, journey, 0);
            JourneySchedule afternoon = createSchedule(102L, journey, 0);
            JourneySchedule nextDay = createSchedule(103L, journey, 1);

            givenActiveMember(journeyId, userId, journey);
            when(journeyScheduleRepository.findActiveSchedulesByJourneyId(journeyId))
                    .thenReturn(List.of(morning, afternoon, nextDay));

            JourneyScheduleListResponse response = journeyScheduleService.retrieveSchedules(journeyId, userId);

            assertThat(response.days().get(0).schedules()).hasSize(2);
            assertThat(response.days().get(1).schedules()).hasSize(1);
            assertThat(response.days().get(2).schedules()).isEmpty();
        }

        @Test
        @DisplayName("active member가 아니면 일정 목록을 조회할 수 없다")
        void inactiveMemberCannotGetSchedules() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);

            when(journeyRepository.getByIdWithPostContextOrThrow(journeyId)).thenReturn(journey);
            when(journeyMemberRepository.existsByJourneyIdAndUserIdAndStatus(journeyId, userId, JourneyMemberStatus.ACTIVE))
                    .thenReturn(false);

            assertThatThrownBy(() -> journeyScheduleService.retrieveSchedules(journeyId, userId))
                    .isInstanceOf(JourneyAccessDeniedException.class);

            verify(journeyScheduleRepository, never()).findActiveSchedulesByJourneyId(any());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 일정 생성
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("일정 생성")
    class CreateSchedule {

        @Test
        @DisplayName("active member는 여행 기간 내 일정을 생성할 수 있다")
        void activeMemberCanCreateSchedule() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);
            JourneyScheduleCreateRequest request = new JourneyScheduleCreateRequest(
                    "오사카 성 관광",
                    ScheduleCategory.SIGHTSEEING,
                    0,
                    START_TIME,
                    END_TIME,
                    "오사카 성",
                    "입장권 미리 구매"
            );

            givenActiveMember(journeyId, userId, journey);
            when(journeyScheduleRepository.saveAndFlush(any(JourneySchedule.class))).thenAnswer(invocation -> {
                JourneySchedule saved = invocation.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 101L);
                return saved;
            });
            when(journeyScheduleRepository.findActiveSchedulesByJourneyId(journeyId)).thenReturn(List.of());

            JourneyScheduleListResponse response = journeyScheduleService.createSchedule(journeyId, userId, request);

            assertThat(response.journeyId()).isEqualTo(journeyId);

            ArgumentCaptor<JourneySchedule> captor = ArgumentCaptor.forClass(JourneySchedule.class);
            verify(journeyScheduleRepository).saveAndFlush(captor.capture());
            JourneySchedule saved = captor.getValue();
            assertThat(saved.getTitle()).isEqualTo("오사카 성 관광");
            assertThat(saved.getCategory()).isEqualTo(ScheduleCategory.SIGHTSEEING);
            assertThat(saved.getDayOffset()).isEqualTo(0);
            assertThat(saved.getStartTime()).isEqualTo(START_TIME);
            assertThat(saved.getEndTime()).isEqualTo(END_TIME);
            assertThat(saved.getPlaceName()).isEqualTo("오사카 성");
            assertThat(saved.getMemo()).isEqualTo("입장권 미리 구매");
        }

        @Test
        @DisplayName("카테고리 없이도 일정을 생성할 수 있다")
        void canCreateScheduleWithoutCategory() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);
            JourneyScheduleCreateRequest request = new JourneyScheduleCreateRequest(
                    "자유 일정", null, 0, START_TIME, END_TIME, "도톤보리", null
            );

            givenActiveMember(journeyId, userId, journey);
            when(journeyScheduleRepository.saveAndFlush(any())).thenAnswer(inv -> {
                ReflectionTestUtils.setField((JourneySchedule) inv.getArgument(0), "id", 1L);
                return inv.getArgument(0);
            });
            when(journeyScheduleRepository.findActiveSchedulesByJourneyId(journeyId)).thenReturn(List.of());

            JourneyScheduleListResponse response = journeyScheduleService.createSchedule(journeyId, userId, request);

            assertThat(response).isNotNull();
            ArgumentCaptor<JourneySchedule> captor = ArgumentCaptor.forClass(JourneySchedule.class);
            verify(journeyScheduleRepository).saveAndFlush(captor.capture());
            assertThat(captor.getValue().getCategory()).isNull();
        }

        @Test
        @DisplayName("여행 첫째 날(dayOffset=0)과 마지막 날 일차는 허용된다")
        void boundaryDatesAreAllowed() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);

            givenActiveMember(journeyId, userId, journey);
            when(journeyScheduleRepository.saveAndFlush(any())).thenAnswer(inv -> {
                ReflectionTestUtils.setField((JourneySchedule) inv.getArgument(0), "id", 1L);
                return inv.getArgument(0);
            });
            when(journeyScheduleRepository.findActiveSchedulesByJourneyId(journeyId)).thenReturn(List.of());

            JourneyScheduleCreateRequest startRequest = new JourneyScheduleCreateRequest(
                    "시작일 일정", null, 0, START_TIME, END_TIME, "장소A", null
            );
            assertThat(journeyScheduleService.createSchedule(journeyId, userId, startRequest)).isNotNull();

            JourneyScheduleCreateRequest endRequest = new JourneyScheduleCreateRequest(
                    "종료일 일정", null, 2, START_TIME, END_TIME, "장소B", null
            );
            assertThat(journeyScheduleService.createSchedule(journeyId, userId, endRequest)).isNotNull();
        }

        @Test
        @DisplayName("dayOffset이 음수이면 예외가 발생한다")
        void beforeStartDateThrowsException() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);
            JourneyScheduleCreateRequest request = new JourneyScheduleCreateRequest(
                    "전날 일정", null, -1, START_TIME, END_TIME, "장소", null
            );

            givenActiveMember(journeyId, userId, journey);

            assertThatThrownBy(() -> journeyScheduleService.createSchedule(journeyId, userId, request))
                    .isInstanceOf(InvalidJourneyScheduleException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_SCHEDULE_INVALID_DATE);

            verify(journeyScheduleRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("dayOffset이 여행 기간을 초과하면 예외가 발생한다")
        void afterEndDateThrowsException() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);
            JourneyScheduleCreateRequest request = new JourneyScheduleCreateRequest(
                    "다음날 일정", null, 3, START_TIME, END_TIME, "장소", null
            );

            givenActiveMember(journeyId, userId, journey);

            assertThatThrownBy(() -> journeyScheduleService.createSchedule(journeyId, userId, request))
                    .isInstanceOf(InvalidJourneyScheduleException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_SCHEDULE_INVALID_DATE);

            verify(journeyScheduleRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("시작 시간만 설정하고 종료 시간 없이 생성할 수 있다")
        void canCreateWithStartTimeOnly() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);
            JourneyScheduleCreateRequest request = new JourneyScheduleCreateRequest(
                    "시작만 있는 일정", null, 0, START_TIME, null, "장소", null
            );

            givenActiveMember(journeyId, userId, journey);
            when(journeyScheduleRepository.saveAndFlush(any())).thenAnswer(inv -> {
                ReflectionTestUtils.setField((JourneySchedule) inv.getArgument(0), "id", 1L);
                return inv.getArgument(0);
            });
            when(journeyScheduleRepository.findActiveSchedulesByJourneyId(journeyId)).thenReturn(List.of());

            assertThat(journeyScheduleService.createSchedule(journeyId, userId, request)).isNotNull();
        }

        @Test
        @DisplayName("시작 시간과 종료 시간 모두 없이 생성할 수 있다")
        void canCreateWithNoTime() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);
            JourneyScheduleCreateRequest request = new JourneyScheduleCreateRequest(
                    "시간 미정 일정", null, 0, null, null, "장소", null
            );

            givenActiveMember(journeyId, userId, journey);
            when(journeyScheduleRepository.saveAndFlush(any())).thenAnswer(inv -> {
                ReflectionTestUtils.setField((JourneySchedule) inv.getArgument(0), "id", 1L);
                return inv.getArgument(0);
            });
            when(journeyScheduleRepository.findActiveSchedulesByJourneyId(journeyId)).thenReturn(List.of());

            assertThat(journeyScheduleService.createSchedule(journeyId, userId, request)).isNotNull();
        }

        @Test
        @DisplayName("시작 시간 없이 종료 시간만 설정하면 예외가 발생한다")
        void endTimeWithoutStartTimeThrowsException() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);
            JourneyScheduleCreateRequest request = new JourneyScheduleCreateRequest(
                    "잘못된 일정", null, 0, null, END_TIME, "장소", null
            );

            givenActiveMember(journeyId, userId, journey);

            assertThatThrownBy(() -> journeyScheduleService.createSchedule(journeyId, userId, request))
                    .isInstanceOf(InvalidJourneyScheduleException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_SCHEDULE_INVALID_TIME);

            verify(journeyScheduleRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("active member가 아니면 일정을 생성할 수 없다")
        void inactiveMemberCannotCreateSchedule() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);
            JourneyScheduleCreateRequest request = new JourneyScheduleCreateRequest(
                    "관광 일정", null, 0, START_TIME, END_TIME, "장소", null
            );

            when(journeyRepository.getByIdWithPostContextOrThrow(journeyId)).thenReturn(journey);
            when(journeyMemberRepository.existsByJourneyIdAndUserIdAndStatus(journeyId, userId, JourneyMemberStatus.ACTIVE))
                    .thenReturn(false);

            assertThatThrownBy(() -> journeyScheduleService.createSchedule(journeyId, userId, request))
                    .isInstanceOf(JourneyAccessDeniedException.class);

            verify(journeyScheduleRepository, never()).saveAndFlush(any());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 일정 수정
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("일정 수정")
    class UpdateSchedule {

        @Test
        @DisplayName("title을 공백 문자열로 보내면 예외가 발생한다")
        void blankTitleThrowsException() {
            Long journeyId = 1L;
            Long userId = 10L;
            Long scheduleId = 101L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);
            JourneySchedule schedule = createSchedule(scheduleId, journey, 0);
            JourneyScheduleUpdateRequest request = new JourneyScheduleUpdateRequest(
                    "   ", null, null, null, null, null, null, null, null
            );

            givenActiveMember(journeyId, userId, journey);
            when(journeyScheduleRepository.getActiveByIdAndJourneyIdOrThrow(scheduleId, journeyId))
                    .thenReturn(schedule);

            assertThatThrownBy(() -> journeyScheduleService.updateSchedule(journeyId, scheduleId, userId, request))
                    .isInstanceOf(InvalidJourneyScheduleException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_SCHEDULE_INVALID_TITLE);
        }

        @Test
        @DisplayName("placeName을 공백 문자열로 보내면 예외가 발생한다")
        void blankPlaceNameThrowsException() {
            Long journeyId = 1L;
            Long userId = 10L;
            Long scheduleId = 101L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);
            JourneySchedule schedule = createSchedule(scheduleId, journey, 0);
            JourneyScheduleUpdateRequest request = new JourneyScheduleUpdateRequest(
                    null, null, null, null, null, null, null, "  ", null
            );

            givenActiveMember(journeyId, userId, journey);
            when(journeyScheduleRepository.getActiveByIdAndJourneyIdOrThrow(scheduleId, journeyId))
                    .thenReturn(schedule);

            assertThatThrownBy(() -> journeyScheduleService.updateSchedule(journeyId, scheduleId, userId, request))
                    .isInstanceOf(InvalidJourneyScheduleException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_SCHEDULE_INVALID_PLACE_NAME);
        }

        @Test
        @DisplayName("active member는 일정을 수정할 수 있다")
        void activeMemberCanUpdateSchedule() {
            Long journeyId = 1L;
            Long userId = 10L;
            Long scheduleId = 101L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);
            JourneySchedule schedule = createSchedule(scheduleId, journey, 0);
            JourneyScheduleUpdateRequest request = new JourneyScheduleUpdateRequest(
                    "수정된 제목",
                    ScheduleCategory.CAFE,
                    null,
                    LocalTime.of(10, 0),
                    null,
                    LocalTime.of(12, 0),
                    null,
                    null,
                    null
            );

            givenActiveMember(journeyId, userId, journey);
            when(journeyScheduleRepository.getActiveByIdAndJourneyIdOrThrow(scheduleId, journeyId))
                    .thenReturn(schedule);
            when(journeyScheduleRepository.findActiveSchedulesByJourneyId(journeyId))
                    .thenReturn(List.of(schedule));

            JourneyScheduleListResponse response = journeyScheduleService.updateSchedule(
                    journeyId, scheduleId, userId, request
            );

            assertThat(schedule.getTitle()).isEqualTo("수정된 제목");
            assertThat(schedule.getCategory()).isEqualTo(ScheduleCategory.CAFE);
            assertThat(schedule.getStartTime()).isEqualTo(LocalTime.of(10, 0));
            assertThat(schedule.getEndTime()).isEqualTo(LocalTime.of(12, 0));
            assertThat(response.journeyId()).isEqualTo(journeyId);
            verify(journeyScheduleRepository).flush();
        }

        @Test
        @DisplayName("일정을 생성하지 않은 다른 active member도 수정할 수 있다")
        void otherActiveMemberCanUpdateSchedule() {
            Long journeyId = 1L;
            Long creatorId = 10L;
            Long otherMemberId = 20L;
            Journey journey = createJourney(journeyId, creatorId, START_DATE, END_DATE);
            JourneySchedule schedule = createSchedule(101L, journey, 0);
            JourneyScheduleUpdateRequest request = new JourneyScheduleUpdateRequest(
                    "다른 멤버가 수정", null, null, null, null, null, null, null, null
            );

            givenActiveMember(journeyId, otherMemberId, journey);
            when(journeyScheduleRepository.getActiveByIdAndJourneyIdOrThrow(101L, journeyId))
                    .thenReturn(schedule);
            when(journeyScheduleRepository.findActiveSchedulesByJourneyId(journeyId)).thenReturn(List.of(schedule));

            journeyScheduleService.updateSchedule(journeyId, 101L, otherMemberId, request);

            assertThat(schedule.getTitle()).isEqualTo("다른 멤버가 수정");
        }

        @Test
        @DisplayName("일차를 변경할 때 여행 기간 외 일차면 예외가 발생한다")
        void updateToOutOfRangeDateThrowsException() {
            Long journeyId = 1L;
            Long userId = 10L;
            Long scheduleId = 101L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);
            JourneySchedule schedule = createSchedule(scheduleId, journey, 0);
            JourneyScheduleUpdateRequest request = new JourneyScheduleUpdateRequest(
                    null, null, 3, null, null, null, null, null, null
            );

            givenActiveMember(journeyId, userId, journey);
            when(journeyScheduleRepository.getActiveByIdAndJourneyIdOrThrow(scheduleId, journeyId))
                    .thenReturn(schedule);

            assertThatThrownBy(() -> journeyScheduleService.updateSchedule(journeyId, scheduleId, userId, request))
                    .isInstanceOf(InvalidJourneyScheduleException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_SCHEDULE_INVALID_DATE);

            verify(journeyScheduleRepository, never()).flush();
        }

        @Test
        @DisplayName("일차를 변경하지 않으면 기존 일차로 범위 검증을 통과한다")
        void noDateChangePassesValidation() {
            Long journeyId = 1L;
            Long userId = 10L;
            Long scheduleId = 101L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);
            JourneySchedule schedule = createSchedule(scheduleId, journey, 0);
            JourneyScheduleUpdateRequest request = new JourneyScheduleUpdateRequest(
                    "수정된 제목", null, null, null, null, null, null, null, null
            );

            givenActiveMember(journeyId, userId, journey);
            when(journeyScheduleRepository.getActiveByIdAndJourneyIdOrThrow(scheduleId, journeyId))
                    .thenReturn(schedule);
            when(journeyScheduleRepository.findActiveSchedulesByJourneyId(journeyId)).thenReturn(List.of(schedule));

            assertThat(journeyScheduleService.updateSchedule(journeyId, scheduleId, userId, request)).isNotNull();
            assertThat(schedule.getTitle()).isEqualTo("수정된 제목");
        }

        @Test
        @DisplayName("memo가 빈 문자열이면 null로 초기화된다")
        void emptyMemoIsCleared() {
            Long journeyId = 1L;
            Long userId = 10L;
            Long scheduleId = 101L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);
            JourneySchedule schedule = createSchedule(scheduleId, journey, 0);
            JourneyScheduleUpdateRequest request = new JourneyScheduleUpdateRequest(
                    null, null, null, null, null, null, null, null, ""
            );

            givenActiveMember(journeyId, userId, journey);
            when(journeyScheduleRepository.getActiveByIdAndJourneyIdOrThrow(scheduleId, journeyId))
                    .thenReturn(schedule);
            when(journeyScheduleRepository.findActiveSchedulesByJourneyId(journeyId)).thenReturn(List.of());

            journeyScheduleService.updateSchedule(journeyId, scheduleId, userId, request);

            assertThat(schedule.getMemo()).isNull();
        }

        @Test
        @DisplayName("수정할 필드가 모두 null이면 예외가 발생한다")
        void allNullFieldsThrowsException() {
            Long journeyId = 1L;
            Long userId = 10L;
            Long scheduleId = 101L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);
            JourneySchedule schedule = createSchedule(scheduleId, journey, 0);
            JourneyScheduleUpdateRequest request = new JourneyScheduleUpdateRequest(
                    null, null, null, null, null, null, null, null, null
            );

            givenActiveMember(journeyId, userId, journey);
            when(journeyScheduleRepository.getActiveByIdAndJourneyIdOrThrow(scheduleId, journeyId))
                    .thenReturn(schedule);

            assertThatThrownBy(() -> journeyScheduleService.updateSchedule(journeyId, scheduleId, userId, request))
                    .isInstanceOf(InvalidJourneyScheduleException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_SCHEDULE_EMPTY_PATCH);

            verify(journeyScheduleRepository, never()).flush();
        }

        @Test
        @DisplayName("존재하지 않는 일정 ID면 예외가 발생한다")
        void notFoundScheduleThrowsException() {
            Long journeyId = 1L;
            Long userId = 10L;
            Long scheduleId = 999L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);
            JourneyScheduleUpdateRequest request = new JourneyScheduleUpdateRequest(
                    "제목", null, null, null, null, null, null, null, null
            );

            givenActiveMember(journeyId, userId, journey);
            when(journeyScheduleRepository.getActiveByIdAndJourneyIdOrThrow(scheduleId, journeyId))
                    .thenThrow(new JourneyScheduleNotFoundException());

            assertThatThrownBy(() -> journeyScheduleService.updateSchedule(journeyId, scheduleId, userId, request))
                    .isInstanceOf(JourneyScheduleNotFoundException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_SCHEDULE_NOT_FOUND);
        }

        @Test
        @DisplayName("기존에 시작 시간이 없는 일정에 종료 시간만 추가하면 예외가 발생한다")
        void addEndTimeToScheduleWithoutStartTimeThrowsException() {
            Long journeyId = 1L;
            Long userId = 10L;
            Long scheduleId = 101L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);
            JourneySchedule schedule = createScheduleWithNoTime(scheduleId, journey, 0);
            JourneyScheduleUpdateRequest request = new JourneyScheduleUpdateRequest(
                    null, null, null, null, null, END_TIME, null, null, null
            );

            givenActiveMember(journeyId, userId, journey);
            when(journeyScheduleRepository.getActiveByIdAndJourneyIdOrThrow(scheduleId, journeyId))
                    .thenReturn(schedule);

            assertThatThrownBy(() -> journeyScheduleService.updateSchedule(journeyId, scheduleId, userId, request))
                    .isInstanceOf(InvalidJourneyScheduleException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_SCHEDULE_INVALID_TIME);

            verify(journeyScheduleRepository, never()).flush();
        }

        @Test
        @DisplayName("clearEndTime=true이면 종료 시간이 null로 초기화된다")
        void clearEndTimeResetsEndTime() {
            Long journeyId = 1L;
            Long userId = 10L;
            Long scheduleId = 101L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);
            JourneySchedule schedule = createSchedule(scheduleId, journey, 0);
            JourneyScheduleUpdateRequest request = new JourneyScheduleUpdateRequest(
                    null, null, null, null, null, null, true, null, null
            );

            givenActiveMember(journeyId, userId, journey);
            when(journeyScheduleRepository.getActiveByIdAndJourneyIdOrThrow(scheduleId, journeyId))
                    .thenReturn(schedule);
            when(journeyScheduleRepository.findActiveSchedulesByJourneyId(journeyId)).thenReturn(List.of());

            journeyScheduleService.updateSchedule(journeyId, scheduleId, userId, request);

            assertThat(schedule.getEndTime()).isNull();
            assertThat(schedule.getStartTime()).isEqualTo(START_TIME);
        }

        @Test
        @DisplayName("clearStartTime=true이면 시작/종료 시간이 함께 null로 초기화된다")
        void clearStartTimeClearsStartAndEndTime() {
            Long journeyId = 1L;
            Long userId = 10L;
            Long scheduleId = 101L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);
            JourneySchedule schedule = createSchedule(scheduleId, journey, 0);
            JourneyScheduleUpdateRequest request = new JourneyScheduleUpdateRequest(
                    null, null, null, null, true, null, true, null, null
            );

            givenActiveMember(journeyId, userId, journey);
            when(journeyScheduleRepository.getActiveByIdAndJourneyIdOrThrow(scheduleId, journeyId))
                    .thenReturn(schedule);
            when(journeyScheduleRepository.findActiveSchedulesByJourneyId(journeyId)).thenReturn(List.of());

            journeyScheduleService.updateSchedule(journeyId, scheduleId, userId, request);

            assertThat(schedule.getStartTime()).isNull();
            assertThat(schedule.getEndTime()).isNull();
        }

        @Test
        @DisplayName("종료 시간이 남아 있는 상태에서 시작 시간만 클리어하면 예외가 발생한다")
        void clearStartTimeWithRemainingEndTimeThrowsException() {
            Long journeyId = 1L;
            Long userId = 10L;
            Long scheduleId = 101L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);
            JourneySchedule schedule = createSchedule(scheduleId, journey, 0);
            JourneyScheduleUpdateRequest request = new JourneyScheduleUpdateRequest(
                    null, null, null, null, true, null, null, null, null
            );

            givenActiveMember(journeyId, userId, journey);
            when(journeyScheduleRepository.getActiveByIdAndJourneyIdOrThrow(scheduleId, journeyId))
                    .thenReturn(schedule);

            assertThatThrownBy(() -> journeyScheduleService.updateSchedule(journeyId, scheduleId, userId, request))
                    .isInstanceOf(InvalidJourneyScheduleException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_SCHEDULE_INVALID_TIME);

            verify(journeyScheduleRepository, never()).flush();
        }

        @Test
        @DisplayName("active member가 아니면 일정을 수정할 수 없다")
        void inactiveMemberCannotUpdateSchedule() {
            Long journeyId = 1L;
            Long userId = 10L;
            Long scheduleId = 101L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);
            JourneyScheduleUpdateRequest request = new JourneyScheduleUpdateRequest(
                    "제목", null, null, null, null, null, null, null, null
            );

            when(journeyRepository.getByIdWithPostContextOrThrow(journeyId)).thenReturn(journey);
            when(journeyMemberRepository.existsByJourneyIdAndUserIdAndStatus(journeyId, userId, JourneyMemberStatus.ACTIVE))
                    .thenReturn(false);

            assertThatThrownBy(() -> journeyScheduleService.updateSchedule(journeyId, scheduleId, userId, request))
                    .isInstanceOf(JourneyAccessDeniedException.class);

            verify(journeyScheduleRepository, never()).flush();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 일정 삭제
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("일정 삭제")
    class DeleteSchedule {

        @Test
        @DisplayName("active member는 일정을 soft delete 처리할 수 있다")
        void activeMemberCanDeleteSchedule() {
            Long journeyId = 1L;
            Long userId = 10L;
            Long scheduleId = 101L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);
            JourneySchedule schedule = createSchedule(scheduleId, journey, 0);

            givenActiveMember(journeyId, userId, journey);
            when(journeyScheduleRepository.getActiveByIdAndJourneyIdOrThrow(scheduleId, journeyId))
                    .thenReturn(schedule);
            when(timeProvider.now()).thenReturn(NOW);

            journeyScheduleService.deleteSchedule(journeyId, scheduleId, userId);

            assertThat(schedule.isDeleted()).isTrue();
            assertThat(schedule.getDeletedBy()).isEqualTo(userId);
            assertThat(schedule.getDeletedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("일정을 생성하지 않은 다른 active member도 삭제할 수 있다")
        void otherActiveMemberCanDeleteSchedule() {
            Long journeyId = 1L;
            Long creatorId = 10L;
            Long otherMemberId = 20L;
            Journey journey = createJourney(journeyId, creatorId, START_DATE, END_DATE);
            JourneySchedule schedule = createSchedule(101L, journey, 0);

            givenActiveMember(journeyId, otherMemberId, journey);
            when(journeyScheduleRepository.getActiveByIdAndJourneyIdOrThrow(101L, journeyId))
                    .thenReturn(schedule);
            when(timeProvider.now()).thenReturn(NOW);

            journeyScheduleService.deleteSchedule(journeyId, 101L, otherMemberId);

            assertThat(schedule.isDeleted()).isTrue();
            assertThat(schedule.getDeletedBy()).isEqualTo(otherMemberId);
        }

        @Test
        @DisplayName("존재하지 않는 일정 ID면 예외가 발생한다")
        void notFoundScheduleThrowsException() {
            Long journeyId = 1L;
            Long userId = 10L;
            Long scheduleId = 999L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);

            givenActiveMember(journeyId, userId, journey);
            when(journeyScheduleRepository.getActiveByIdAndJourneyIdOrThrow(scheduleId, journeyId))
                    .thenThrow(new JourneyScheduleNotFoundException());

            assertThatThrownBy(() -> journeyScheduleService.deleteSchedule(journeyId, scheduleId, userId))
                    .isInstanceOf(JourneyScheduleNotFoundException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_SCHEDULE_NOT_FOUND);
        }

        @Test
        @DisplayName("active member가 아니면 일정을 삭제할 수 없다")
        void inactiveMemberCannotDeleteSchedule() {
            Long journeyId = 1L;
            Long userId = 10L;
            Long scheduleId = 101L;
            Journey journey = createJourney(journeyId, userId, START_DATE, END_DATE);

            when(journeyRepository.getByIdWithPostContextOrThrow(journeyId)).thenReturn(journey);
            when(journeyMemberRepository.existsByJourneyIdAndUserIdAndStatus(journeyId, userId, JourneyMemberStatus.ACTIVE))
                    .thenReturn(false);

            assertThatThrownBy(() -> journeyScheduleService.deleteSchedule(journeyId, scheduleId, userId))
                    .isInstanceOf(JourneyAccessDeniedException.class);

            verify(journeyScheduleRepository, never()).getActiveByIdAndJourneyIdOrThrow(any(), any());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 헬퍼 메서드
    // ─────────────────────────────────────────────────────────────

    private void givenActiveMember(Long journeyId, Long userId, Journey journey) {
        when(journeyRepository.getByIdWithPostContextOrThrow(journeyId)).thenReturn(journey);
        when(journeyMemberRepository.existsByJourneyIdAndUserIdAndStatus(journeyId, userId, JourneyMemberStatus.ACTIVE))
                .thenReturn(true);
    }

    private Journey createJourney(Long journeyId, Long ownerId, LocalDate startDate, LocalDate endDate) {
        Post post = createPost(100L, ownerId, startDate, endDate);
        Journey journey = Journey.create(post);
        ReflectionTestUtils.setField(journey, "id", journeyId);
        return journey;
    }

    private JourneySchedule createSchedule(Long scheduleId, Journey journey, int dayOffset) {
        JourneySchedule schedule = JourneySchedule.create(
                journey,
                "기존 일정 제목",
                ScheduleCategory.SIGHTSEEING,
                dayOffset,
                START_TIME,
                END_TIME,
                "기존 장소",
                "기존 메모"
        );
        ReflectionTestUtils.setField(schedule, "id", scheduleId);
        return schedule;
    }

    private JourneySchedule createScheduleWithNoTime(Long scheduleId, Journey journey, int dayOffset) {
        JourneySchedule schedule = JourneySchedule.create(
                journey,
                "시간 미정 일정",
                null,
                dayOffset,
                null,
                null,
                "기존 장소",
                null
        );
        ReflectionTestUtils.setField(schedule, "id", scheduleId);
        return schedule;
    }

    private Post createPost(Long postId, Long ownerId, LocalDate startDate, LocalDate endDate) {
        User owner = createUser(ownerId, "owner-" + ownerId);
        Destination destination = Destination.builder()
                .countryCode("JP")
                .countryName("일본")
                .city("오사카")
                .build();

        Post post = Post.createPost(
                owner,
                destination,
                "오사카 3일 여행",
                "일정 서비스 테스트용 본문입니다. 충분한 길이로 작성합니다.",
                startDate,
                endDate,
                4,
                startDate.minusDays(1),
                Gender.U,
                true,
                null,
                null,
                null,
                "[]",
                CompanionType.FULL
        );
        ReflectionTestUtils.setField(post, "id", postId);
        return post;
    }

    private User createUser(Long userId, String name) {
        User user = User.builder()
                .email(name + "@example.com")
                .name(name)
                .oauthId("oauth-" + userId)
                .oauthType(OauthType.KAKAO)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);
        Profile profile = new Profile(user);
        ReflectionTestUtils.setField(profile, "nickname", name);
        ReflectionTestUtils.setField(user, "profile", profile);
        return user;
    }
}
