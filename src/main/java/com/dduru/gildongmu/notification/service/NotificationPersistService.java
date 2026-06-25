package com.dduru.gildongmu.notification.service;

import com.dduru.gildongmu.notification.domain.Notification;
import com.dduru.gildongmu.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class NotificationPersistService {

    private final NotificationRepository notificationRepository;

    public void save(Notification notification) {
        notificationRepository.save(notification);
    }

    public void saveAll(List<Notification> notifications) {
        notificationRepository.saveAll(notifications);
    }
}
