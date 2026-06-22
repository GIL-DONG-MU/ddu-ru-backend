package com.dduru.gildongmu.notification.service;

import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.notification.domain.Notification;
import com.dduru.gildongmu.notification.domain.enums.NotificationType;
import com.dduru.gildongmu.notification.domain.enums.ResourceType;
import com.dduru.gildongmu.notification.dto.response.NotificationReadResponse;
import com.dduru.gildongmu.notification.exception.NotificationAccessDeniedException;
import com.dduru.gildongmu.notification.exception.NotificationNotFoundException;
import com.dduru.gildongmu.notification.repository.NotificationRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService 테스트")
class NotificationServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 22, 10, 0);

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private TimeProvider timeProvider;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        lenient().when(timeProvider.now()).thenReturn(NOW);
        notificationService = new NotificationService(notificationRepository, timeProvider);
    }

    @Nested
    @DisplayName("단건 읽음 처리")
    class MarkAsRead {

        @Test
        @DisplayName("미읽음 알림을 읽음 처리하면 read=true, readAt이 기록되고 success=true를 반환한다")
        void unreadNotificationIsMarkedAsRead() {
            Long userId = 1L;
            Long notificationId = 10L;
            Notification notification = createUnreadNotification(notificationId, createUser(userId));

            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

            NotificationReadResponse response = notificationService.markAsRead(userId, notificationId);

            assertThat(response.success()).isTrue();
            assertThat(notification.isRead()).isTrue();
            assertThat(notification.getReadAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("이미 읽은 알림을 재요청해도 예외 없이 success=true를 반환한다 (멱등)")
        void alreadyReadNotificationIsIdempotent() {
            Long userId = 1L;
            Long notificationId = 10L;
            Notification notification = createReadNotification(notificationId, createUser(userId));
            LocalDateTime originalReadAt = notification.getReadAt();

            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

            NotificationReadResponse response = notificationService.markAsRead(userId, notificationId);

            assertThat(response.success()).isTrue();
            assertThat(notification.getReadAt()).isEqualTo(originalReadAt); // readAt 변경 없음
        }

        @Test
        @DisplayName("존재하지 않는 알림 ID로 요청하면 NotificationNotFoundException이 발생한다")
        void notFoundNotificationThrowsException() {
            when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> notificationService.markAsRead(1L, 99L))
                    .isInstanceOf(NotificationNotFoundException.class);
        }

        @Test
        @DisplayName("타인의 알림을 읽음 처리하려 하면 NotificationAccessDeniedException이 발생한다")
        void otherUsersNotificationThrowsAccessDeniedException() {
            Long ownerId = 1L;
            Long attackerId = 2L;
            Long notificationId = 10L;
            Notification notification = createUnreadNotification(notificationId, createUser(ownerId));

            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

            assertThatThrownBy(() -> notificationService.markAsRead(attackerId, notificationId))
                    .isInstanceOf(NotificationAccessDeniedException.class);

            assertThat(notification.isRead()).isFalse(); // 상태 변경 없음
        }

        @Test
        @DisplayName("접근 권한이 없으면 알림 상태는 변경되지 않는다")
        void accessDeniedDoesNotMutateNotificationState() {
            Long ownerId = 1L;
            Long notificationId = 10L;
            Notification notification = createUnreadNotification(notificationId, createUser(ownerId));

            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

            try {
                notificationService.markAsRead(99L, notificationId);
            } catch (NotificationAccessDeniedException ignored) {
            }

            verify(timeProvider, never()).now();
        }
    }

    @Nested
    @DisplayName("전체 읽음 처리")
    class MarkAllAsRead {

        @Test
        @DisplayName("전체 읽음 처리하면 벌크 업데이트 쿼리를 실행하고 success=true를 반환한다")
        void bulkUpdateIsCalledAndReturnsSuccess() {
            Long userId = 1L;

            NotificationReadResponse response = notificationService.markAllAsRead(userId);

            assertThat(response.success()).isTrue();
            verify(notificationRepository).markAllAsReadByRecipientId(userId, NOW);
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

    private Notification createUnreadNotification(Long notificationId, User recipient) {
        Notification notification = Notification.create(
                recipient,
                NotificationType.MATCH_APPLIED,
                "[테스트 게시글]에 새로운 참여 신청이 도착했습니다.",
                ResourceType.MATCH,
                1L
        );
        ReflectionTestUtils.setField(notification, "id", notificationId);
        return notification;
    }

    private Notification createReadNotification(Long notificationId, User recipient) {
        Notification notification = createUnreadNotification(notificationId, recipient);
        notification.markAsRead(LocalDateTime.of(2026, 6, 1, 9, 0));
        return notification;
    }
}
