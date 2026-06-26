package com.dduru.gildongmu.fcm.service;

import com.dduru.gildongmu.fcm.domain.UserFcmToken;
import com.dduru.gildongmu.fcm.repository.UserFcmTokenRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmPushService {

    private static final int FCM_MAX_TOKENS = 500; // FCM API 단일 요청 토큰 수 제한

    private final UserFcmTokenRepository userFcmTokenRepository;

    @Async("fcmExecutor")
    @Transactional
    public void sendToUser(Long userId, String title, String body) {
        if (isFirebaseNotInitialized()) return;

        List<String> tokens = userFcmTokenRepository.findAllByUserId(userId)
                .stream()
                .map(UserFcmToken::getToken)
                .toList();

        if (tokens.isEmpty()) return;

        sendMulticast(tokens, title, body);
    }

    @Async("fcmExecutor")
    @Transactional
    public void sendToUsers(List<Long> userIds, String title, String body) {
        if (isFirebaseNotInitialized()) return;
        if (userIds.isEmpty()) return;

        List<String> tokens = userFcmTokenRepository.findAllByUserIdIn(userIds)
                .stream()
                .map(UserFcmToken::getToken)
                .toList();

        if (tokens.isEmpty()) return;

        sendMulticast(tokens, title, body);
    }

    private void sendMulticast(List<String> tokens, String title, String body) {
        for (int i = 0; i < tokens.size(); i += FCM_MAX_TOKENS) {
            List<String> chunk = tokens.subList(i, Math.min(i + FCM_MAX_TOKENS, tokens.size()));
            sendBatch(chunk, title, body);
        }
    }

    private void sendBatch(List<String> tokens, String title, String body) {
        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .addAllTokens(tokens)
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            handleFailedTokens(tokens, response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 멀티캐스트 발송 실패 - tokens={}", tokens.size(), e);
        }
    }

    private void handleFailedTokens(List<String> tokens, BatchResponse response) {
        List<SendResponse> responses = response.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);
            if (!sendResponse.isSuccessful()) {
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

    private boolean isFirebaseNotInitialized() {
        return FirebaseApp.getApps().isEmpty();
    }
}
