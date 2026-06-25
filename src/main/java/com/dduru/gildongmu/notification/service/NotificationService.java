package com.dduru.gildongmu.notification.service;

import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.notification.domain.Notification;
import com.dduru.gildongmu.notification.dto.response.NotificationReadResponse;
import com.dduru.gildongmu.notification.exception.NotificationAccessDeniedException;
import com.dduru.gildongmu.notification.exception.NotificationNotFoundException;
import com.dduru.gildongmu.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final TimeProvider timeProvider;

    public NotificationReadResponse markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(NotificationNotFoundException::new);

        if (!notification.getRecipient().getId().equals(userId)) {
            throw new NotificationAccessDeniedException();
        }

        if (!notification.isRead()) {
            notification.markAsRead(timeProvider.now());
        }

        return NotificationReadResponse.ok();
    }

    public NotificationReadResponse markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByRecipientId(userId, timeProvider.now());
        return NotificationReadResponse.ok();
    }
}
