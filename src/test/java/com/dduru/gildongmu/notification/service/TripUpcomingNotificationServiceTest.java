package com.dduru.gildongmu.notification.service;

import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.fcm.service.FcmPushService;
import com.dduru.gildongmu.journey.dto.query.UpcomingTripMemberQueryResult;
import com.dduru.gildongmu.journey.repository.JourneyMemberRepository;
import com.dduru.gildongmu.notification.domain.Notification;
import com.dduru.gildongmu.notification.domain.enums.NotificationType;
import com.dduru.gildongmu.notification.domain.enums.ResourceType;
import com.dduru.gildongmu.notification.repository.NotificationRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import com.dduru.gildongmu.user.repository.UserRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TripUpcomingNotificationService 테스트")
class TripUpcomingNotificationServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 30);
    private static final LocalDate TARGET_DATE = TODAY.plusDays(3);

    @Mock private JourneyMemberRepository journeyMemberRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationPersistService notificationPersistService;
    @Mock private UserRepository userRepository;
    @Mock private FcmPushService fcmPushService;
    @Mock private TimeProvider timeProvider;

    private TripUpcomingNotificationService service;

    @BeforeEach
    void setUp() {
        when(timeProvider.today()).thenReturn(TODAY);
        service = new TripUpcomingNotificationService(
                journeyMemberRepository, notificationRepository, notificationPersistService,
                userRepository, fcmPushService, timeProvider
        );
    }

    @Nested
    @DisplayName("대상 여정이 없는 경우")
    class NoUpcomingTrips {

        @Test
        @DisplayName("3일 후 출발 여정이 없으면 알림을 저장하지 않는다")
        void skipsWhenNoUpcomingTrips() {
            when(journeyMemberRepository.findUpcomingTripMembers(TARGET_DATE)).thenReturn(List.of());

            service.notifyUpcomingTrips();

            verify(notificationPersistService, never()).saveAll(any());
            verify(fcmPushService, never()).sendToUsers(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("정상 발송")
    class NormalNotification {

        @Test
        @DisplayName("3일 후 출발 여정의 ACTIVE 멤버 전원에게 TRIP_UPCOMING 알림을 저장한다")
        void savesNotificationsForAllActiveMembers() {
            Long journeyId = 1L;
            List<UpcomingTripMemberQueryResult> rows = List.of(
                    new UpcomingTripMemberQueryResult(journeyId, "도쿄 여행", 10L),
                    new UpcomingTripMemberQueryResult(journeyId, "도쿄 여행", 20L)
            );

            when(journeyMemberRepository.findUpcomingTripMembers(TARGET_DATE)).thenReturn(rows);
            when(notificationRepository.findNotifiedRecipientIds(
                    eq(NotificationType.TRIP_UPCOMING), eq(journeyId), any())).thenReturn(List.of());
            when(userRepository.getReferenceById(10L)).thenReturn(createUser(10L));
            when(userRepository.getReferenceById(20L)).thenReturn(createUser(20L));
            when(userRepository.findEnabledUserIds(List.of(10L, 20L))).thenReturn(List.of(10L, 20L));

            service.notifyUpcomingTrips();

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
            verify(notificationPersistService).saveAll(captor.capture());

            List<Notification> saved = captor.getValue();
            assertThat(saved).hasSize(2);
            assertThat(saved).allSatisfy(n -> {
                assertThat(n.getType()).isEqualTo(NotificationType.TRIP_UPCOMING);
                assertThat(n.getResourceType()).isEqualTo(ResourceType.JOURNEY);
                assertThat(n.getResourceId()).isEqualTo(journeyId);
                assertThat(n.isRead()).isFalse();
            });
        }

        @Test
        @DisplayName("알림 off 유저는 FCM 발송 대상에서 제외된다")
        void fcmIsFilteredByNotificationEnabled() {
            Long journeyId = 1L;
            List<UpcomingTripMemberQueryResult> rows = List.of(
                    new UpcomingTripMemberQueryResult(journeyId, "도쿄 여행", 10L),
                    new UpcomingTripMemberQueryResult(journeyId, "도쿄 여행", 20L)
            );

            when(journeyMemberRepository.findUpcomingTripMembers(TARGET_DATE)).thenReturn(rows);
            when(notificationRepository.findNotifiedRecipientIds(
                    eq(NotificationType.TRIP_UPCOMING), eq(journeyId), any())).thenReturn(List.of());
            when(userRepository.getReferenceById(10L)).thenReturn(createUser(10L));
            when(userRepository.getReferenceById(20L)).thenReturn(createUser(20L));
            when(userRepository.findEnabledUserIds(List.of(10L, 20L))).thenReturn(List.of(10L));

            service.notifyUpcomingTrips();

            verify(fcmPushService).sendToUsers(eq(List.of(10L)), any(), any());
        }

        @Test
        @DisplayName("전원 알림 off이면 FCM을 발송하지 않는다")
        void skipsFcmWhenAllUsersDisabled() {
            Long journeyId = 1L;
            List<UpcomingTripMemberQueryResult> rows = List.of(
                    new UpcomingTripMemberQueryResult(journeyId, "도쿄 여행", 10L)
            );

            when(journeyMemberRepository.findUpcomingTripMembers(TARGET_DATE)).thenReturn(rows);
            when(notificationRepository.findNotifiedRecipientIds(
                    eq(NotificationType.TRIP_UPCOMING), eq(journeyId), any())).thenReturn(List.of());
            when(userRepository.getReferenceById(10L)).thenReturn(createUser(10L));
            when(userRepository.findEnabledUserIds(List.of(10L))).thenReturn(List.of());

            service.notifyUpcomingTrips();

            verify(notificationPersistService).saveAll(any());
            verify(fcmPushService, never()).sendToUsers(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("중복 발송 방지")
    class DuplicatePrevention {

        @Test
        @DisplayName("오늘 이미 전원 발송 완료된 여정은 스킵한다")
        void skipsJourneyAlreadySentToday() {
            Long journeyId = 1L;
            List<UpcomingTripMemberQueryResult> rows = List.of(
                    new UpcomingTripMemberQueryResult(journeyId, "도쿄 여행", 10L)
            );

            when(journeyMemberRepository.findUpcomingTripMembers(TARGET_DATE)).thenReturn(rows);
            when(notificationRepository.findNotifiedRecipientIds(
                    eq(NotificationType.TRIP_UPCOMING), eq(journeyId), any())).thenReturn(List.of(10L));

            service.notifyUpcomingTrips();

            verify(notificationPersistService, never()).saveAll(any());
            verify(fcmPushService, never()).sendToUsers(any(), any(), any());
        }

        @Test
        @DisplayName("배치 실패로 일부만 발송된 경우 미발송 멤버에게만 재발송한다")
        void resendsOnlyToMembersWhoDidNotReceive() {
            Long journeyId = 1L;
            List<UpcomingTripMemberQueryResult> rows = List.of(
                    new UpcomingTripMemberQueryResult(journeyId, "도쿄 여행", 10L),
                    new UpcomingTripMemberQueryResult(journeyId, "도쿄 여행", 20L)
            );

            when(journeyMemberRepository.findUpcomingTripMembers(TARGET_DATE)).thenReturn(rows);
            // 10L은 이미 받음, 20L은 아직 미수신
            when(notificationRepository.findNotifiedRecipientIds(
                    eq(NotificationType.TRIP_UPCOMING), eq(journeyId), any())).thenReturn(List.of(10L));
            when(userRepository.getReferenceById(20L)).thenReturn(createUser(20L));
            when(userRepository.findEnabledUserIds(List.of(20L))).thenReturn(List.of(20L));

            service.notifyUpcomingTrips();

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
            verify(notificationPersistService).saveAll(captor.capture());
            assertThat(captor.getValue()).hasSize(1);
            assertThat(captor.getValue().get(0).getResourceId()).isEqualTo(journeyId);
        }

        @Test
        @DisplayName("여러 여정 중 일부만 중복이면 미발송 여정만 정상 발송한다")
        void sendsOnlyToNonDuplicateJourneys() {
            Long journeyId1 = 1L;
            Long journeyId2 = 2L;
            List<UpcomingTripMemberQueryResult> rows = List.of(
                    new UpcomingTripMemberQueryResult(journeyId1, "도쿄 여행", 10L),
                    new UpcomingTripMemberQueryResult(journeyId2, "오사카 여행", 20L)
            );

            when(journeyMemberRepository.findUpcomingTripMembers(TARGET_DATE)).thenReturn(rows);
            when(notificationRepository.findNotifiedRecipientIds(
                    eq(NotificationType.TRIP_UPCOMING), eq(journeyId1), any())).thenReturn(List.of(10L));
            when(notificationRepository.findNotifiedRecipientIds(
                    eq(NotificationType.TRIP_UPCOMING), eq(journeyId2), any())).thenReturn(List.of());
            when(userRepository.getReferenceById(20L)).thenReturn(createUser(20L));
            when(userRepository.findEnabledUserIds(List.of(20L))).thenReturn(List.of(20L));

            service.notifyUpcomingTrips();

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
            verify(notificationPersistService).saveAll(captor.capture());
            assertThat(captor.getValue()).hasSize(1);
            assertThat(captor.getValue().get(0).getResourceId()).isEqualTo(journeyId2);
        }
    }

    // ── 헬퍼 메서드 ──────────────────────────────────────────────────────────

    private User createUser(Long userId) {
        User user = User.builder()
                .email("user" + userId + "@example.com")
                .name("user" + userId)
                .oauthId("oauth-" + userId)
                .oauthType(OauthType.KAKAO)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }
}
