package com.dduru.gildongmu.superhost.scheduler;

import com.dduru.gildongmu.superhost.service.SuperHostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuperHostScheduler {

    private final SuperHostService superHostService;

    @Scheduled(cron = "0 */10 * * * *")
    public void endExpiredExposures() {
        try {
            int updatedCount = superHostService.endExpiredExposures();
            if (updatedCount > 0) {
                log.info("만료된 슈퍼호스트 노출 종료 처리 - count={}", updatedCount);
            }
        } catch (Exception e) {
            log.error("슈퍼호스트 만료 스케줄러 실패", e);
        }
    }
}
