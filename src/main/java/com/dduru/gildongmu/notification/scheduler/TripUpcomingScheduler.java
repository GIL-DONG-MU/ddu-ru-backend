package com.dduru.gildongmu.notification.scheduler;

import com.dduru.gildongmu.notification.service.TripUpcomingNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TripUpcomingScheduler {

    private final TripUpcomingNotificationService tripUpcomingNotificationService;

    @Scheduled(cron = "0 0 9 * * *") // 매일 오전 9시 실행
    public void notifyUpcomingTrips() {
        try {
            tripUpcomingNotificationService.notifyUpcomingTrips();
            log.info("여행 임박 알림 발송 완료");
        } catch (Exception e) {
            log.error("여행 임박 알림 스케줄러 실패", e);
        }
    }
}
