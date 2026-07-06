package com.dduru.gildongmu.fcm.service;

import com.dduru.gildongmu.fcm.domain.UserFcmToken;
import com.dduru.gildongmu.fcm.domain.enums.DeviceType;
import com.dduru.gildongmu.fcm.repository.UserFcmTokenRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import com.google.firebase.FirebaseApp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FcmPushService 테스트")
class FcmPushServiceTest {

    @Mock
    private UserFcmTokenRepository userFcmTokenRepository;

    private FcmPushService fcmPushService;

    @BeforeEach
    void setUp() {
        fcmPushService = new FcmPushService(userFcmTokenRepository);
    }

    @Nested
    @DisplayName("단일 사용자 푸시 발송")
    class SendToUser {

        @Test
        @DisplayName("Firebase가 초기화되지 않으면 토큰 조회 없이 종료한다")
        void skipsWhenFirebaseNotInitialized() {
            try (MockedStatic<FirebaseApp> firebaseAppMock = mockStatic(FirebaseApp.class)) {
                firebaseAppMock.when(FirebaseApp::getApps).thenReturn(List.of());

                fcmPushService.sendToUser(1L, "제목", "내용", Map.of("resourceType", "JOURNEY", "resourceId", "1"));

                verify(userFcmTokenRepository, never()).findAllByUserId(any());
            }
        }

        @Test
        @DisplayName("Firebase가 초기화됐으나 등록된 토큰이 없으면 발송하지 않는다")
        void skipsWhenNoTokensRegistered() {
            try (MockedStatic<FirebaseApp> firebaseAppMock = mockStatic(FirebaseApp.class)) {
                firebaseAppMock.when(FirebaseApp::getApps).thenReturn(List.of(mock(FirebaseApp.class)));
                when(userFcmTokenRepository.findAllByUserId(1L)).thenReturn(List.of());

                fcmPushService.sendToUser(1L, "제목", "내용", Map.of("resourceType", "JOURNEY", "resourceId", "1"));

                verify(userFcmTokenRepository).findAllByUserId(1L);
            }
        }
    }

    @Nested
    @DisplayName("다수 사용자 푸시 발송")
    class SendToUsers {

        @Test
        @DisplayName("Firebase가 초기화되지 않으면 토큰 조회 없이 종료한다")
        void skipsWhenFirebaseNotInitialized() {
            try (MockedStatic<FirebaseApp> firebaseAppMock = mockStatic(FirebaseApp.class)) {
                firebaseAppMock.when(FirebaseApp::getApps).thenReturn(List.of());

                fcmPushService.sendToUsers(List.of(1L, 2L), "제목", "내용", null, Map.of());

                verify(userFcmTokenRepository, never()).findAllByUserIdIn(any());
            }
        }

        @Test
        @DisplayName("userIds가 비어 있으면 토큰 조회 없이 종료한다")
        void skipsWhenUserIdsIsEmpty() {
            fcmPushService.sendToUsers(List.of(), "제목", "내용", null, Map.of());

            verify(userFcmTokenRepository, never()).findAllByUserIdIn(any());
        }

        @Test
        @DisplayName("Firebase가 초기화됐으나 모든 사용자의 토큰이 없으면 발송하지 않는다")
        void skipsWhenNoTokensForAnyUser() {
            try (MockedStatic<FirebaseApp> firebaseAppMock = mockStatic(FirebaseApp.class)) {
                firebaseAppMock.when(FirebaseApp::getApps).thenReturn(List.of(mock(FirebaseApp.class)));
                when(userFcmTokenRepository.findAllByUserIdIn(List.of(1L, 2L))).thenReturn(List.of());

                fcmPushService.sendToUsers(List.of(1L, 2L), "제목", "내용", null, Map.of());

                verify(userFcmTokenRepository).findAllByUserIdIn(List.of(1L, 2L));
            }
        }

        @Test
        @DisplayName("Firebase가 초기화되면 모든 사용자의 토큰을 수집한다")
        void collectsTokensFromAllUsers() {
            try (MockedStatic<FirebaseApp> firebaseAppMock = mockStatic(FirebaseApp.class)) {
                firebaseAppMock.when(FirebaseApp::getApps).thenReturn(List.of(mock(FirebaseApp.class)));

                UserFcmToken token1 = createFcmToken(1L, "token-user1");
                UserFcmToken token2 = createFcmToken(2L, "token-user2");
                when(userFcmTokenRepository.findAllByUserIdIn(List.of(1L, 2L)))
                        .thenReturn(List.of(token1, token2));

                try {
                    fcmPushService.sendToUsers(List.of(1L, 2L), "제목", "내용", null, Map.of());
                } catch (Exception ignored) {
                    // Firebase 미초기화 환경에서 sendEachForMulticast 호출 시 발생하는 예외 무시
                }

                verify(userFcmTokenRepository).findAllByUserIdIn(List.of(1L, 2L));
            }
        }
    }

    private UserFcmToken createFcmToken(Long userId, String token) {
        User user = User.builder()
                .email("user" + userId + "@example.com")
                .name("user" + userId)
                .oauthId("oauth-" + userId)
                .oauthType(OauthType.KAKAO)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);
        return UserFcmToken.create(user, token, DeviceType.AOS);
    }
}
