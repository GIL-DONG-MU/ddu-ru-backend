package com.dduru.gildongmu.fcm.service;

import com.dduru.gildongmu.fcm.domain.enums.DeviceType;
import com.dduru.gildongmu.fcm.dto.request.FcmTokenDeleteRequest;
import com.dduru.gildongmu.fcm.dto.request.FcmTokenRegisterRequest;
import com.dduru.gildongmu.fcm.repository.UserFcmTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("FcmTokenService 테스트")
class FcmTokenServiceTest {

    @Mock
    private UserFcmTokenRepository userFcmTokenRepository;

    private FcmTokenService fcmTokenService;

    @BeforeEach
    void setUp() {
        fcmTokenService = new FcmTokenService(userFcmTokenRepository);
    }

    @Nested
    @DisplayName("토큰 등록/갱신")
    class Register {

        @Test
        @DisplayName("userId, token, deviceType을 upsert 쿼리에 전달한다")
        void callsUpsertWithCorrectArguments() {
            Long userId = 1L;
            FcmTokenRegisterRequest request = new FcmTokenRegisterRequest("fcm-token-abc", DeviceType.AOS);

            fcmTokenService.register(userId, request);

            verify(userFcmTokenRepository).upsert(userId, "fcm-token-abc", "AOS");
        }
    }

    @Nested
    @DisplayName("토큰 삭제 (로그아웃)")
    class Delete {

        @Test
        @DisplayName("userId와 token으로 해당 기기 토큰을 삭제한다")
        void callsDeleteWithCorrectArguments() {
            Long userId = 1L;
            FcmTokenDeleteRequest request = new FcmTokenDeleteRequest("fcm-token-abc");

            fcmTokenService.delete(userId, request);

            verify(userFcmTokenRepository).deleteByUser_IdAndToken(userId, "fcm-token-abc");
        }
    }
}
