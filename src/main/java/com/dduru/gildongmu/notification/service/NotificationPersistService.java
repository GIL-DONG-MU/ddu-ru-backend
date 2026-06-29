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
// AFTER_COMMIT 리스너와 같은 빈에 두면 프록시를 타지 않아 REQUIRES_NEW가 적용되지 않으므로 별도 빈으로 분리
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
