package com.dduru.gildongmu.journey.service;

import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.ChatRoomMember;
import com.dduru.gildongmu.chat.domain.enums.ChatMemberRole;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.repository.ChatRoomMemberRepository;
import com.dduru.gildongmu.chat.repository.ChatRoomRepository;
import com.dduru.gildongmu.chat.service.ChatMessageSendService;
import com.dduru.gildongmu.common.config.S3Properties;
import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.common.validation.S3ImageUrlValidator;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.journey.domain.JourneyMember;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberRole;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import com.dduru.gildongmu.journey.domain.enums.JourneyRoleType;
import com.dduru.gildongmu.journey.dto.request.JourneyMemberRoleUpdateRequest;
import com.dduru.gildongmu.journey.dto.response.RoleLabelInfo;
import com.dduru.gildongmu.journey.dto.request.JourneyUpdateRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyMemberRoleResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyUpdateResponse;
import com.dduru.gildongmu.journey.exception.InvalidJourneyBasicInfoException;
import com.dduru.gildongmu.journey.exception.InvalidJourneyMemberRoleException;
import com.dduru.gildongmu.journey.exception.JourneyAccessDeniedException;
import com.dduru.gildongmu.journey.exception.JourneyMemberNotFoundException;
import com.dduru.gildongmu.journey.exception.JourneyMemberRemovalException;
import com.dduru.gildongmu.journey.domain.JourneySchedule;
import com.dduru.gildongmu.journey.domain.enums.ScheduleCategory;
import com.dduru.gildongmu.journey.repository.JourneyMemberRepository;
import com.dduru.gildongmu.journey.repository.JourneyRepository;
import com.dduru.gildongmu.journey.repository.JourneyScheduleRepository;
import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import com.dduru.gildongmu.participation.repository.ParticipationRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JourneyService 테스트")
class JourneyServiceTest {
    private static final String S3_HOST = "https://dummy-bucket.s3.ap-northeast-2.amazonaws.com";

    @Mock
    private JourneyRepository journeyRepository;
    @Mock
    private JourneyMemberRepository journeyMemberRepository;
    @Mock
    private JourneyScheduleRepository journeyScheduleRepository;
    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;
    @Mock
    private ParticipationRepository participationRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private ChatMessageSendService chatMessageSendService;
    @Mock
    private TimeProvider timeProvider;

    private JourneyService journeyService;

    @BeforeEach
    void setUp() {
        S3Properties s3Properties = new S3Properties();
        s3Properties.setBucket("dummy-bucket");
        s3Properties.setRegion("ap-northeast-2");
        journeyService = new JourneyService(
                journeyRepository,
                journeyMemberRepository,
                journeyScheduleRepository,
                chatRoomRepository,
                chatRoomMemberRepository,
                participationRepository,
                postRepository,
                chatMessageSendService,
                new S3ImageUrlValidator(s3Properties),
                timeProvider
        );
    }

    @Nested
    @DisplayName("나의 여정 기본 정보 수정")
    class UpdateBasicInfo {

        @Test
        @DisplayName("호스트는 제목과 대표 사진을 수정할 수 있다")
        void hostCanUpdateJourneyBasicInfo() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId);
            JourneyUpdateRequest request = new JourneyUpdateRequest(
                    "제주 우리 여행",
                    S3_HOST + "/journeys/journey-updated.png",
                    null,
                    null
            );

            when(journeyRepository.findUpdatableJourneyByIdAndUserId(journeyId, userId, JourneyMemberStatus.ACTIVE, JourneyMemberRole.HOST))
                    .thenReturn(Optional.of(journey));

            JourneyUpdateResponse response = journeyService.updateBasicInfo(journeyId, userId, request);

            assertThat(response.journeyId()).isEqualTo(journeyId);
            assertThat(response.title()).isEqualTo("제주 우리 여행");
            assertThat(response.photoUrl()).isEqualTo(S3_HOST + "/journeys/journey-updated.png");
            assertThat(journey.getTitle()).isEqualTo("제주 우리 여행");
            assertThat(journey.getPhotoUrl()).isEqualTo(S3_HOST + "/journeys/journey-updated.png");
        }

        @Test
        @DisplayName("호스트가 아닌 active 멤버는 수정할 수 없다")
        void nonHostActiveMemberCannotUpdateJourneyBasicInfo() {
            Long journeyId = 1L;
            Long userId = 20L;
            JourneyUpdateRequest request = new JourneyUpdateRequest(
                    "멤버가 바꾼 제목",
                    S3_HOST + "/journeys/member-updated.png",
                    null,
                    null
            );

            when(journeyRepository.findUpdatableJourneyByIdAndUserId(journeyId, userId, JourneyMemberStatus.ACTIVE, JourneyMemberRole.HOST))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> journeyService.updateBasicInfo(journeyId, userId, request))
                    .isInstanceOf(JourneyAccessDeniedException.class);
        }

        @Test
        @DisplayName("비활성 멤버는 수정할 수 없다")
        void inactiveMemberCannotUpdateJourneyBasicInfo() {
            Long journeyId = 1L;
            Long userId = 20L;
            Journey journey = createJourney(journeyId, 10L);
            JourneyMember member = JourneyMember.createMember(journey, createUser(userId, "member"), LocalDateTime.now());
            member.remove(LocalDateTime.now());
            JourneyUpdateRequest request = new JourneyUpdateRequest("새 제목", null, null, null);

            when(journeyRepository.findUpdatableJourneyByIdAndUserId(journeyId, userId, JourneyMemberStatus.ACTIVE, JourneyMemberRole.HOST))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> journeyService.updateBasicInfo(journeyId, userId, request))
                    .isInstanceOf(JourneyAccessDeniedException.class);
        }

        @Test
        @DisplayName("수정 값이 모두 null이면 예외가 발생한다")
        void throwsWhenNoPatchValueProvided() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId);
            JourneyUpdateRequest request = new JourneyUpdateRequest(null, null, null, null);

            when(journeyRepository.findUpdatableJourneyByIdAndUserId(journeyId, userId, JourneyMemberStatus.ACTIVE, JourneyMemberRole.HOST))
                    .thenReturn(Optional.of(journey));

            assertThatThrownBy(() -> journeyService.updateBasicInfo(journeyId, userId, request))
                    .isInstanceOf(InvalidJourneyBasicInfoException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_EMPTY_PATCH);
        }

        @Test
        @DisplayName("제목 길이가 범위를 벗어나면 예외가 발생한다")
        void throwsWhenTitleLengthIsInvalid() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId);
            JourneyUpdateRequest request = new JourneyUpdateRequest("😀😀😀😀", null, null, null);

            when(journeyRepository.findUpdatableJourneyByIdAndUserId(journeyId, userId, JourneyMemberStatus.ACTIVE, JourneyMemberRole.HOST))
                    .thenReturn(Optional.of(journey));

            assertThatThrownBy(() -> journeyService.updateBasicInfo(journeyId, userId, request))
                    .isInstanceOf(InvalidJourneyBasicInfoException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_INVALID_TITLE_LENGTH);
        }

        @Test
        @DisplayName("대표 사진 URL 형식이 잘못되면 예외가 발생한다")
        void throwsWhenPhotoUrlIsInvalid() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId);
            JourneyUpdateRequest request = new JourneyUpdateRequest(null, "http:test", null, null);

            when(journeyRepository.findUpdatableJourneyByIdAndUserId(journeyId, userId, JourneyMemberStatus.ACTIVE, JourneyMemberRole.HOST))
                    .thenReturn(Optional.of(journey));

            assertThatThrownBy(() -> journeyService.updateBasicInfo(journeyId, userId, request))
                    .isInstanceOf(InvalidJourneyBasicInfoException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_INVALID_PHOTO_URL);
        }

        @Test
        @DisplayName("대표 사진만 수정할 수 있다")
        void canUpdateOnlyPhoto() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId);
            JourneyUpdateRequest request = new JourneyUpdateRequest(
                    null,
                    S3_HOST + "/journeys/journey-photo-only.png",
                    null,
                    null
            );

            when(journeyRepository.findUpdatableJourneyByIdAndUserId(journeyId, userId, JourneyMemberStatus.ACTIVE, JourneyMemberRole.HOST))
                    .thenReturn(Optional.of(journey));

            JourneyUpdateResponse response = journeyService.updateBasicInfo(journeyId, userId, request);

            assertThat(response.title()).isEqualTo("제주도 2박 3일 여행");
            assertThat(response.photoUrl()).isEqualTo(S3_HOST + "/journeys/journey-photo-only.png");
        }

        @Test
        @DisplayName("여행 시작일과 종료일을 수정할 수 있다")
        void canUpdateTravelDates() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId);
            LocalDate newStart = LocalDate.now().plusDays(10);
            LocalDate newEnd = LocalDate.now().plusDays(13);
            JourneyUpdateRequest request = new JourneyUpdateRequest(null, null, newStart, newEnd);

            when(journeyRepository.findUpdatableJourneyByIdAndUserId(journeyId, userId, JourneyMemberStatus.ACTIVE, JourneyMemberRole.HOST))
                    .thenReturn(Optional.of(journey));
            when(journeyScheduleRepository.findActiveSchedulesWithDayOffsetGreaterThanOrEqual(journeyId, 4))
                    .thenReturn(List.of());

            JourneyUpdateResponse response = journeyService.updateBasicInfo(journeyId, userId, request);

            assertThat(response.startDate()).isEqualTo(newStart);
            assertThat(response.endDate()).isEqualTo(newEnd);
            assertThat(journey.getPost().getStartDate()).isEqualTo(newStart);
            assertThat(journey.getPost().getEndDate()).isEqualTo(newEnd);
            assertThat(journey.getPost().getRecruitDeadline()).isEqualTo(newEnd.minusDays(1));
        }

        @Test
        @DisplayName("여행 날짜 수정 시 당일치기이면 recruitDeadline이 startDate 하루 전으로 갱신된다")
        void updatesRecruitDeadlineForSameDayTrip() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId);
            LocalDate date = LocalDate.now().plusDays(10);
            JourneyUpdateRequest request = new JourneyUpdateRequest(null, null, date, date);

            when(journeyRepository.findUpdatableJourneyByIdAndUserId(journeyId, userId, JourneyMemberStatus.ACTIVE, JourneyMemberRole.HOST))
                    .thenReturn(Optional.of(journey));
            when(journeyScheduleRepository.findActiveSchedulesWithDayOffsetGreaterThanOrEqual(journeyId, 1))
                    .thenReturn(List.of());

            journeyService.updateBasicInfo(journeyId, userId, request);

            assertThat(journey.getPost().getRecruitDeadline()).isEqualTo(date.minusDays(1));
        }

        @Test
        @DisplayName("여행 날짜와 제목을 함께 수정할 수 있다")
        void canUpdateTravelDatesWithTitle() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId);
            LocalDate newStart = LocalDate.now().plusDays(10);
            LocalDate newEnd = LocalDate.now().plusDays(12);
            JourneyUpdateRequest request = new JourneyUpdateRequest("새로운 여행 제목", null, newStart, newEnd);

            when(journeyRepository.findUpdatableJourneyByIdAndUserId(journeyId, userId, JourneyMemberStatus.ACTIVE, JourneyMemberRole.HOST))
                    .thenReturn(Optional.of(journey));
            when(journeyScheduleRepository.findActiveSchedulesWithDayOffsetGreaterThanOrEqual(journeyId, 3))
                    .thenReturn(List.of());

            JourneyUpdateResponse response = journeyService.updateBasicInfo(journeyId, userId, request);

            assertThat(response.title()).isEqualTo("새로운 여행 제목");
            assertThat(response.startDate()).isEqualTo(newStart);
            assertThat(response.endDate()).isEqualTo(newEnd);
        }

        @Test
        @DisplayName("시작일만 입력하면 예외가 발생한다")
        void throwsWhenOnlyStartDateProvided() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId);
            JourneyUpdateRequest request = new JourneyUpdateRequest(null, null, LocalDate.now().plusDays(10), null);

            when(journeyRepository.findUpdatableJourneyByIdAndUserId(journeyId, userId, JourneyMemberStatus.ACTIVE, JourneyMemberRole.HOST))
                    .thenReturn(Optional.of(journey));

            assertThatThrownBy(() -> journeyService.updateBasicInfo(journeyId, userId, request))
                    .isInstanceOf(InvalidJourneyBasicInfoException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_INCOMPLETE_TRAVEL_DATE);
        }

        @Test
        @DisplayName("종료일만 입력하면 예외가 발생한다")
        void throwsWhenOnlyEndDateProvided() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId);
            JourneyUpdateRequest request = new JourneyUpdateRequest(null, null, null, LocalDate.now().plusDays(10));

            when(journeyRepository.findUpdatableJourneyByIdAndUserId(journeyId, userId, JourneyMemberStatus.ACTIVE, JourneyMemberRole.HOST))
                    .thenReturn(Optional.of(journey));

            assertThatThrownBy(() -> journeyService.updateBasicInfo(journeyId, userId, request))
                    .isInstanceOf(InvalidJourneyBasicInfoException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_INCOMPLETE_TRAVEL_DATE);
        }

        @Test
        @DisplayName("시작일과 종료일이 같으면 당일치기로 허용된다")
        void allowsSameDayTrip() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId);
            LocalDate date = LocalDate.now().plusDays(10);
            JourneyUpdateRequest request = new JourneyUpdateRequest(null, null, date, date);

            when(journeyRepository.findUpdatableJourneyByIdAndUserId(journeyId, userId, JourneyMemberStatus.ACTIVE, JourneyMemberRole.HOST))
                    .thenReturn(Optional.of(journey));
            when(journeyScheduleRepository.findActiveSchedulesWithDayOffsetGreaterThanOrEqual(journeyId, 1))
                    .thenReturn(List.of());

            JourneyUpdateResponse response = journeyService.updateBasicInfo(journeyId, userId, request);

            assertThat(response.startDate()).isEqualTo(date);
            assertThat(response.endDate()).isEqualTo(date);
        }

        @Test
        @DisplayName("시작일이 종료일 이후이면 예외가 발생한다")
        void throwsWhenStartDateIsAfterEndDate() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId);
            JourneyUpdateRequest request = new JourneyUpdateRequest(
                    null, null,
                    LocalDate.now().plusDays(12),
                    LocalDate.now().plusDays(10)
            );

            when(journeyRepository.findUpdatableJourneyByIdAndUserId(journeyId, userId, JourneyMemberStatus.ACTIVE, JourneyMemberRole.HOST))
                    .thenReturn(Optional.of(journey));

            assertThatThrownBy(() -> journeyService.updateBasicInfo(journeyId, userId, request))
                    .isInstanceOf(InvalidJourneyBasicInfoException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_INVALID_TRAVEL_DATE);
        }

        @Test
        @DisplayName("여행 기간이 줄어들면 범위 밖 일정이 자동 소프트 삭제된다")
        void outOfRangeSchedulesAreAutoDeleted() {
            Long journeyId = 1L;
            Long userId = 10L;
            Journey journey = createJourney(journeyId, userId);
            // 기존 3박 4일(dayOffset 0~3) → 2박 3일(dayOffset 0~2)로 축소
            LocalDate newStart = LocalDate.now().plusDays(5);
            LocalDate newEnd = LocalDate.now().plusDays(7); // DAYS.between = 2, newTotalDays = 3
            JourneyUpdateRequest request = new JourneyUpdateRequest(null, null, newStart, newEnd);

            JourneySchedule outOfRangeSchedule = createSchedule(journey, 3);
            LocalDateTime deletedAt = LocalDateTime.of(2026, 6, 9, 12, 0);

            when(journeyRepository.findUpdatableJourneyByIdAndUserId(journeyId, userId, JourneyMemberStatus.ACTIVE, JourneyMemberRole.HOST))
                    .thenReturn(Optional.of(journey));
            when(journeyScheduleRepository.findActiveSchedulesWithDayOffsetGreaterThanOrEqual(journeyId, 3))
                    .thenReturn(List.of(outOfRangeSchedule));
            when(timeProvider.now()).thenReturn(deletedAt);

            journeyService.updateBasicInfo(journeyId, userId, request);

            assertThat(outOfRangeSchedule.isDeleted()).isTrue();
            assertThat(outOfRangeSchedule.getDeletedBy()).isEqualTo(userId);
            assertThat(outOfRangeSchedule.getDeletedAt()).isEqualTo(deletedAt);
        }
    }

    @Nested
    @DisplayName("나의 여정 멤버 관리")
    class ManageMembers {

        @Test
        @DisplayName("호스트는 active 멤버를 내보내고 그룹 채팅방에서도 제거할 수 있다")
        void hostCanRemoveActiveMemberAndSyncGroupChat() {
            Long journeyId = 1L;
            Long hostUserId = 10L;
            Long memberUserId = 20L;
            Long roomId = 55L;
            Journey journey = createJourney(journeyId, hostUserId);
            Post post = journey.getPost();
            User memberUser = createUser(memberUserId, "member");
            JourneyMember member = JourneyMember.createMember(journey, memberUser, LocalDateTime.now());
            Participation participation = Participation.createParticipation(post, memberUser, "같이 가고 싶어요.");
            post.approveParticipation(participation);
            ChatRoom room = createGroupChatRoom(roomId, journey);
            ChatRoomMember chatRoomMember = ChatRoomMember.create(room, memberUser, ChatMemberRole.GUEST);

            when(timeProvider.now()).thenReturn(LocalDateTime.of(2026, 5, 13, 16, 20));
            when(journeyMemberRepository.existsActiveHost(journeyId, hostUserId)).thenReturn(true);
            when(journeyRepository.getPostIdByIdOrThrow(journeyId)).thenReturn(post.getId());
            when(postRepository.getActiveByIdWithLockOrThrow(post.getId())).thenReturn(post);
            when(journeyMemberRepository.findActiveMemberWithLock(journeyId, memberUserId))
                    .thenReturn(Optional.of(member));
            when(participationRepository.findByPostIdAndUserIdAndStatus(post.getId(), memberUserId, ParticipationStatus.APPROVED))
                    .thenReturn(Optional.of(participation));
            when(chatRoomRepository.findByJourneyIdAndRoomType(journeyId, ChatRoomType.GROUP))
                    .thenReturn(Optional.of(room));
            when(chatRoomMemberRepository.findByRoomIdAndUserId(roomId, memberUserId))
                    .thenReturn(Optional.of(chatRoomMember));

            journeyService.removeMember(journeyId, hostUserId, memberUserId);

            assertThat(member.getStatus()).isEqualTo(JourneyMemberStatus.REMOVED);
            assertThat(member.getRemovedAt()).isNotNull();
            assertThat(participation.getStatus()).isEqualTo(ParticipationStatus.APPROVED);
            assertThat(post.getRecruitCount()).isEqualTo(1);
            verify(chatRoomMemberRepository).delete(chatRoomMember);
            verify(chatMessageSendService).publishUserKicked(room, memberUserId, hostUserId);

            InOrder inOrder = inOrder(journeyMemberRepository, postRepository);
            inOrder.verify(journeyMemberRepository).existsActiveHost(journeyId, hostUserId);
            inOrder.verify(postRepository).getActiveByIdWithLockOrThrow(post.getId());
            inOrder.verify(journeyMemberRepository).findActiveMemberWithLock(journeyId, memberUserId);
        }

        @Test
        @DisplayName("active host가 아니면 멤버를 내보낼 수 없다")
        void nonHostCannotRemoveMember() {
            Long journeyId = 1L;
            Long requesterUserId = 30L;
            Long memberUserId = 20L;

            when(journeyMemberRepository.existsActiveHost(journeyId, requesterUserId)).thenReturn(false);

            assertThatThrownBy(() -> journeyService.removeMember(journeyId, requesterUserId, memberUserId))
                    .isInstanceOf(JourneyAccessDeniedException.class);

            verify(journeyMemberRepository, never()).findActiveMemberWithLock(journeyId, memberUserId);
        }

        @Test
        @DisplayName("호스트가 자기 자신을 강퇴하면 전용 에러가 발생한다")
        void cannotRemoveSelf() {
            Long journeyId = 1L;
            Long hostUserId = 10L;

            when(journeyMemberRepository.existsActiveHost(journeyId, hostUserId)).thenReturn(true);

            assertThatThrownBy(() -> journeyService.removeMember(journeyId, hostUserId, hostUserId))
                    .isInstanceOf(JourneyMemberRemovalException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_MEMBER_CANNOT_REMOVE_SELF);

            verify(journeyRepository, never()).getPostIdByIdOrThrow(journeyId);
            verify(journeyMemberRepository, never()).findActiveMemberWithLock(any(), any());
        }
    }

    @Nested
    @DisplayName("나의 여정 멤버 역할 설정")
    class UpdateMemberRole {

        private JourneyMemberRoleUpdateRequest requestOf(JourneyRoleType roleType, String customLabel) {
            return new JourneyMemberRoleUpdateRequest(
                    List.of(new JourneyMemberRoleUpdateRequest.RoleLabelRequest(roleType, customLabel))
            );
        }

        @Test
        @DisplayName("호스트가 멤버에게 단일 역할을 지정할 수 있다")
        void hostCanAssignRoleToMember() {
            Long journeyId = 1L;
            Long hostUserId = 10L;
            Long memberUserId = 20L;
            Journey journey = createJourney(journeyId, hostUserId);
            JourneyMember member = JourneyMember.createMember(journey, createUser(memberUserId, "member"), LocalDateTime.now());
            JourneyMemberRoleUpdateRequest request = requestOf(JourneyRoleType.TREASURER, null);

            when(journeyMemberRepository.existsActiveHost(journeyId, hostUserId)).thenReturn(true);
            when(journeyMemberRepository.findByJourneyIdAndUserId(journeyId, memberUserId)).thenReturn(Optional.of(member));

            JourneyMemberRoleResponse response = journeyService.updateMemberRole(journeyId, hostUserId, memberUserId, request);

            assertThat(response.memberUserId()).isEqualTo(memberUserId);
            assertThat(response.roles()).hasSize(1);
            assertThat(response.roles().get(0).roleType()).isEqualTo(JourneyRoleType.TREASURER);
            assertThat(response.roles().get(0).customRoleLabel()).isNull();
        }

        @Test
        @DisplayName("호스트가 멤버에게 여러 역할을 동시에 지정할 수 있다")
        void hostCanAssignMultipleRoles() {
            Long journeyId = 1L;
            Long hostUserId = 10L;
            Long memberUserId = 20L;
            Journey journey = createJourney(journeyId, hostUserId);
            JourneyMember member = JourneyMember.createMember(journey, createUser(memberUserId, "member"), LocalDateTime.now());
            JourneyMemberRoleUpdateRequest request = new JourneyMemberRoleUpdateRequest(List.of(
                    new JourneyMemberRoleUpdateRequest.RoleLabelRequest(JourneyRoleType.TREASURER, null),
                    new JourneyMemberRoleUpdateRequest.RoleLabelRequest(JourneyRoleType.SCHEDULE, null)
            ));

            when(journeyMemberRepository.existsActiveHost(journeyId, hostUserId)).thenReturn(true);
            when(journeyMemberRepository.findByJourneyIdAndUserId(journeyId, memberUserId)).thenReturn(Optional.of(member));

            JourneyMemberRoleResponse response = journeyService.updateMemberRole(journeyId, hostUserId, memberUserId, request);

            assertThat(response.roles()).hasSize(2);
            assertThat(response.roles().stream().map(RoleLabelInfo::roleType))
                    .containsExactly(JourneyRoleType.TREASURER, JourneyRoleType.SCHEDULE);
        }

        @Test
        @DisplayName("빈 역할 목록을 전달하면 역할이 모두 해제된다")
        void emptyRolesClearsAllRoles() {
            Long journeyId = 1L;
            Long hostUserId = 10L;
            Long memberUserId = 20L;
            Journey journey = createJourney(journeyId, hostUserId);
            JourneyMember member = JourneyMember.createMember(journey, createUser(memberUserId, "member"), LocalDateTime.now());
            JourneyMemberRoleUpdateRequest request = new JourneyMemberRoleUpdateRequest(List.of());

            when(journeyMemberRepository.existsActiveHost(journeyId, hostUserId)).thenReturn(true);
            when(journeyMemberRepository.findByJourneyIdAndUserId(journeyId, memberUserId)).thenReturn(Optional.of(member));

            JourneyMemberRoleResponse response = journeyService.updateMemberRole(journeyId, hostUserId, memberUserId, request);

            assertThat(response.roles()).isEmpty();
        }

        @Test
        @DisplayName("호스트가 CUSTOM 역할과 라벨을 지정할 수 있다")
        void hostCanAssignCustomRole() {
            Long journeyId = 1L;
            Long hostUserId = 10L;
            Long memberUserId = 20L;
            Journey journey = createJourney(journeyId, hostUserId);
            JourneyMember member = JourneyMember.createMember(journey, createUser(memberUserId, "member"), LocalDateTime.now());
            JourneyMemberRoleUpdateRequest request = requestOf(JourneyRoleType.CUSTOM, "식당 예약");

            when(journeyMemberRepository.existsActiveHost(journeyId, hostUserId)).thenReturn(true);
            when(journeyMemberRepository.findByJourneyIdAndUserId(journeyId, memberUserId)).thenReturn(Optional.of(member));

            JourneyMemberRoleResponse response = journeyService.updateMemberRole(journeyId, hostUserId, memberUserId, request);

            assertThat(response.roles()).hasSize(1);
            assertThat(response.roles().get(0).roleType()).isEqualTo(JourneyRoleType.CUSTOM);
            assertThat(response.roles().get(0).customRoleLabel()).isEqualTo("식당 예약");
        }

        @Test
        @DisplayName("CUSTOM 역할에 라벨이 없으면 예외가 발생한다")
        void throwsWhenCustomRoleLabelIsBlank() {
            Long journeyId = 1L;
            Long hostUserId = 10L;
            Long memberUserId = 20L;
            Journey journey = createJourney(journeyId, hostUserId);
            JourneyMember member = JourneyMember.createMember(journey, createUser(memberUserId, "member"), LocalDateTime.now());
            JourneyMemberRoleUpdateRequest request = requestOf(JourneyRoleType.CUSTOM, null);

            when(journeyMemberRepository.existsActiveHost(journeyId, hostUserId)).thenReturn(true);
            when(journeyMemberRepository.findByJourneyIdAndUserId(journeyId, memberUserId)).thenReturn(Optional.of(member));

            assertThatThrownBy(() -> journeyService.updateMemberRole(journeyId, hostUserId, memberUserId, request))
                    .isInstanceOf(InvalidJourneyMemberRoleException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_MEMBER_INVALID_CUSTOM_ROLE_LABEL);
        }

        @Test
        @DisplayName("CUSTOM 역할 라벨이 10자를 초과하면 예외가 발생한다")
        void throwsWhenCustomRoleLabelExceedsMaxLength() {
            Long journeyId = 1L;
            Long hostUserId = 10L;
            Long memberUserId = 20L;
            Journey journey = createJourney(journeyId, hostUserId);
            JourneyMember member = JourneyMember.createMember(journey, createUser(memberUserId, "member"), LocalDateTime.now());
            JourneyMemberRoleUpdateRequest request = requestOf(JourneyRoleType.CUSTOM, "가".repeat(11));

            when(journeyMemberRepository.existsActiveHost(journeyId, hostUserId)).thenReturn(true);
            when(journeyMemberRepository.findByJourneyIdAndUserId(journeyId, memberUserId)).thenReturn(Optional.of(member));

            assertThatThrownBy(() -> journeyService.updateMemberRole(journeyId, hostUserId, memberUserId, request))
                    .isInstanceOf(InvalidJourneyMemberRoleException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_MEMBER_INVALID_CUSTOM_ROLE_LABEL);
        }

        @Test
        @DisplayName("동일한 커스텀 라벨이 중복되면 예외가 발생한다")
        void throwsWhenCustomRoleLabelIsDuplicated() {
            Long journeyId = 1L;
            Long hostUserId = 10L;
            Long memberUserId = 20L;
            Journey journey = createJourney(journeyId, hostUserId);
            JourneyMember member = JourneyMember.createMember(journey, createUser(memberUserId, "member"), LocalDateTime.now());
            JourneyMemberRoleUpdateRequest request = new JourneyMemberRoleUpdateRequest(List.of(
                    new JourneyMemberRoleUpdateRequest.RoleLabelRequest(JourneyRoleType.CUSTOM, "식당 예약"),
                    new JourneyMemberRoleUpdateRequest.RoleLabelRequest(JourneyRoleType.CUSTOM, "식당 예약")
            ));

            when(journeyMemberRepository.existsActiveHost(journeyId, hostUserId)).thenReturn(true);
            when(journeyMemberRepository.findByJourneyIdAndUserId(journeyId, memberUserId)).thenReturn(Optional.of(member));

            assertThatThrownBy(() -> journeyService.updateMemberRole(journeyId, hostUserId, memberUserId, request))
                    .isInstanceOf(InvalidJourneyMemberRoleException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_MEMBER_DUPLICATE_CUSTOM_ROLE_LABEL);
        }

        @Test
        @DisplayName("역할이 6개 이상이면 예외가 발생한다")
        void throwsWhenRoleLimitExceeded() {
            Long journeyId = 1L;
            Long hostUserId = 10L;
            Long memberUserId = 20L;
            Journey journey = createJourney(journeyId, hostUserId);
            JourneyMember member = JourneyMember.createMember(journey, createUser(memberUserId, "member"), LocalDateTime.now());
            JourneyMemberRoleUpdateRequest request = new JourneyMemberRoleUpdateRequest(List.of(
                    new JourneyMemberRoleUpdateRequest.RoleLabelRequest(JourneyRoleType.TREASURER, null),
                    new JourneyMemberRoleUpdateRequest.RoleLabelRequest(JourneyRoleType.SCHEDULE, null),
                    new JourneyMemberRoleUpdateRequest.RoleLabelRequest(JourneyRoleType.PHOTO, null),
                    new JourneyMemberRoleUpdateRequest.RoleLabelRequest(JourneyRoleType.NAVIGATION, null),
                    new JourneyMemberRoleUpdateRequest.RoleLabelRequest(JourneyRoleType.RESERVATION, null),
                    new JourneyMemberRoleUpdateRequest.RoleLabelRequest(JourneyRoleType.CUSTOM, "추가역할")
            ));

            when(journeyMemberRepository.existsActiveHost(journeyId, hostUserId)).thenReturn(true);
            when(journeyMemberRepository.findByJourneyIdAndUserId(journeyId, memberUserId)).thenReturn(Optional.of(member));

            assertThatThrownBy(() -> journeyService.updateMemberRole(journeyId, hostUserId, memberUserId, request))
                    .isInstanceOf(InvalidJourneyMemberRoleException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.JOURNEY_MEMBER_ROLE_LIMIT_EXCEEDED);
        }

        @Test
        @DisplayName("비호스트는 역할을 지정할 수 없다")
        void nonHostCannotAssignRole() {
            Long journeyId = 1L;
            Long requesterUserId = 30L;
            Long memberUserId = 20L;
            JourneyMemberRoleUpdateRequest request = requestOf(JourneyRoleType.TREASURER, null);

            when(journeyMemberRepository.existsActiveHost(journeyId, requesterUserId)).thenReturn(false);

            assertThatThrownBy(() -> journeyService.updateMemberRole(journeyId, requesterUserId, memberUserId, request))
                    .isInstanceOf(JourneyAccessDeniedException.class);

            verify(journeyMemberRepository, never()).findByJourneyIdAndUserId(journeyId, memberUserId);
        }

        @Test
        @DisplayName("존재하지 않는 멤버에게 역할을 지정하면 예외가 발생한다")
        void throwsWhenTargetMemberNotFound() {
            Long journeyId = 1L;
            Long hostUserId = 10L;
            Long memberUserId = 99L;
            JourneyMemberRoleUpdateRequest request = requestOf(JourneyRoleType.PHOTO, null);

            when(journeyMemberRepository.existsActiveHost(journeyId, hostUserId)).thenReturn(true);
            when(journeyMemberRepository.findByJourneyIdAndUserId(journeyId, memberUserId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> journeyService.updateMemberRole(journeyId, hostUserId, memberUserId, request))
                    .isInstanceOf(JourneyMemberNotFoundException.class);
        }

        @Test
        @DisplayName("REMOVED 상태의 멤버에게 역할을 지정하면 예외가 발생한다")
        void throwsWhenTargetMemberIsInactive() {
            Long journeyId = 1L;
            Long hostUserId = 10L;
            Long memberUserId = 20L;
            Journey journey = createJourney(journeyId, hostUserId);
            JourneyMember member = JourneyMember.createMember(journey, createUser(memberUserId, "member"), LocalDateTime.now());
            member.remove(LocalDateTime.now());
            JourneyMemberRoleUpdateRequest request = requestOf(JourneyRoleType.PHOTO, null);

            when(journeyMemberRepository.existsActiveHost(journeyId, hostUserId)).thenReturn(true);
            when(journeyMemberRepository.findByJourneyIdAndUserId(journeyId, memberUserId)).thenReturn(Optional.of(member));

            assertThatThrownBy(() -> journeyService.updateMemberRole(journeyId, hostUserId, memberUserId, request))
                    .isInstanceOf(JourneyMemberNotFoundException.class);
        }
    }

    private Journey createJourney(Long journeyId, Long ownerId) {
        Post post = createPost(100L, ownerId, "제주도 2박 3일 여행", "제주");
        Journey journey = Journey.create(post);
        ReflectionTestUtils.setField(journey, "id", journeyId);
        return journey;
    }

    private Post createPost(Long postId, Long ownerId, String title, String destinationCity) {
        User owner = createUser(ownerId, "owner-" + ownerId);
        Destination destination = Destination.builder()
                .countryCode("KR")
                .countryName("대한민국")
                .city(destinationCity)
                .build();

        Post post = Post.createPost(
                owner,
                destination,
                title,
                "나의 여정 기본 정보 수정 테스트용 본문입니다.",
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(7),
                4,
                LocalDate.now().plusDays(6),
                Gender.U,
                true,
                null,
                null,
                S3_HOST + "/posts/photo.png",
                "[]",
                CompanionType.FULL
        );
        ReflectionTestUtils.setField(post, "id", postId);
        return post;
    }

    private ChatRoom createGroupChatRoom(Long roomId, Journey journey) {
        ChatRoom room = ChatRoom.createGroupChat(journey);
        ReflectionTestUtils.setField(room, "id", roomId);
        return room;
    }

    private User createUser(Long userId, String name) {
        User user = User.builder()
                .email(name + "@example.com")
                .name(name)
                .oauthId("oauth-" + userId)
                .oauthType(OauthType.KAKAO)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }

    private JourneySchedule createSchedule(Journey journey, int dayOffset) {
        return JourneySchedule.create(journey, "테스트 일정", ScheduleCategory.MEAL, dayOffset, null, null, "테스트 장소", null, null);
    }
}
