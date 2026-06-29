package com.dduru.gildongmu.notification.service;

import com.dduru.gildongmu.notification.domain.Notification;
import com.dduru.gildongmu.notification.dto.response.NotificationListResponse;
import com.dduru.gildongmu.notification.dto.response.UnreadCountResponse;
import com.dduru.gildongmu.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;

    public NotificationListResponse getNotifications(Long userId, Long cursor, int size) {
        List<Notification> fetched = notificationRepository.findPageByRecipientId(
                userId, cursor, PageRequest.of(0, size + 1)
        );
        return NotificationListResponse.of(fetched, size);
    }

    public UnreadCountResponse getUnreadCount(Long userId) {
        long count = notificationRepository.countUnreadByRecipientId(userId);
        return new UnreadCountResponse(count);
    }
}
