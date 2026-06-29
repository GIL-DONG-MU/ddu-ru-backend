package com.dduru.gildongmu.notification.service;

import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.notification.domain.Notification;
import com.dduru.gildongmu.notification.dto.response.NotificationReadResponse;
import com.dduru.gildongmu.notification.exception.NotificationAccessDeniedException;
import com.dduru.gildongmu.notification.repository.NotificationRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TimeProvider timeProvider;

    public NotificationReadResponse markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.getByIdOrThrow(notificationId);

        if (notification.isNotOwnedBy(userId)) {
            throw new NotificationAccessDeniedException();
        }

        if (notification.isUnread()) {
            notification.markAsRead(timeProvider.now());
        }

        return NotificationReadResponse.ok();
    }

    public NotificationReadResponse markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByRecipientId(userId, timeProvider.now());
        return NotificationReadResponse.ok();
    }

    public void updateNotificationSettings(Long userId, boolean enabled) {
        User user = userRepository.getByIdOrThrow(userId);
        user.updateNotificationEnabled(enabled);
    }
}
