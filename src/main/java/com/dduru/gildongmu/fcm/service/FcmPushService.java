package com.dduru.gildongmu.fcm.service;

import com.dduru.gildongmu.fcm.domain.UserFcmToken;
import com.dduru.gildongmu.fcm.repository.UserFcmTokenRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.dduru.gildongmu.notification.domain.enums.ResourceType;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmPushService {

    private static final int FCM_MAX_TOKENS = 500; // FCM API 단일 요청 토큰 수 제한

    private final UserFcmTokenRepository userFcmTokenRepository;

    // 앱 딥링크 스펙 계약 키 — 변경 시 앱팀 동기화 필요
    public static Map<String, String> dataPayload(ResourceType resourceType, Long resourceId) {
        return Map.of("resourceType", resourceType.name(), "resourceId", resourceId.toString());
    }

    @Async("fcmExecutor")
    public void sendToUser(Long userId, String title, String body, Map<String, String> data) {
        doSendToUser(userId, title, body, data);
    }

    private void doSendToUser(Long userId, String title, String body, Map<String, String> data) {
        if (isFirebaseNotInitialized()) return;

        List<String> tokens = userFcmTokenRepository.findAllByUserId(userId)
                .stream()
                .map(UserFcmToken::getToken)
                .toList();

        if (tokens.isEmpty()) return;

        sendMulticast(tokens, title, body, null, data);
    }

    @Async("fcmExecutor")
    public void sendToUsers(List<Long> userIds, String title, String body, String collapseKey, Map<String, String> data) {
        doSendToUsers(userIds, title, body, collapseKey, data);
    }

    private void doSendToUsers(List<Long> userIds, String title, String body, String collapseKey, Map<String, String> data) {
        if (isFirebaseNotInitialized()) return;
        if (userIds.isEmpty()) return;

        List<String> tokens = userFcmTokenRepository.findAllByUserIdIn(userIds)
                .stream()
                .map(UserFcmToken::getToken)
                .toList();

        if (tokens.isEmpty()) return;

        sendMulticast(tokens, title, body, collapseKey, data);
    }

    // FCM 단일 요청 토큰 수 제한(500개)으로 인해 청크 단위로 분할 발송
    private void sendMulticast(List<String> tokens, String title, String body, String collapseKey, Map<String, String> data) {
        for (int i = 0; i < tokens.size(); i += FCM_MAX_TOKENS) {
            List<String> chunk = tokens.subList(i, Math.min(i + FCM_MAX_TOKENS, tokens.size()));
            sendBatch(chunk, title, body, collapseKey, data);
        }
    }

    private void sendBatch(List<String> tokens, String title, String body, String collapseKey, Map<String, String> data) {
        MulticastMessage.Builder builder = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .addAllTokens(tokens);

        if (collapseKey != null) {
            builder.setAndroidConfig(AndroidConfig.builder().setCollapseKey(collapseKey).build());
            builder.setApnsConfig(ApnsConfig.builder().putHeader("apns-collapse-id", collapseKey).build());
        }

        if (data != null && !data.isEmpty()) {
            builder.putAllData(data);
        }

        MulticastMessage message = builder.build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            handleFailedTokens(tokens, response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 멀티캐스트 발송 실패 - tokenCount={}", tokens.size(), e);
        } catch (Exception e) {
            log.error("FCM 멀티캐스트 발송 중 예상치 못한 오류 - tokenCount={}", tokens.size(), e);
        }
    }

    private void handleFailedTokens(List<String> tokens, BatchResponse response) {
        List<SendResponse> responses = response.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);
            if (isFailed(sendResponse)) {
                MessagingErrorCode errorCode = sendResponse.getException().getMessagingErrorCode();
                // 네트워크 오류 등 일시적 실패는 토큰을 보존하고, 영구적으로 무효한 토큰만 삭제
                if (errorCode == MessagingErrorCode.UNREGISTERED
                        || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                    String expiredToken = tokens.get(i);
                    userFcmTokenRepository.deleteByToken(expiredToken);
                    log.info("만료된 FCM 토큰 삭제 - token={}", expiredToken);
                }
            }
        }
    }

    private boolean isFailed(SendResponse sendResponse) {
        return !sendResponse.isSuccessful();
    }

    // Firebase 설정 없는 로컬 환경에서 예외 방지
    private boolean isFirebaseNotInitialized() {
        return FirebaseApp.getApps().isEmpty();
    }
}
