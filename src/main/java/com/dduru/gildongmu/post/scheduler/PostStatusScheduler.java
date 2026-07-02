package com.dduru.gildongmu.post.scheduler;

import com.dduru.gildongmu.post.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostStatusScheduler {

    private final PostService postService;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정 실행
    public void updateExpiredPostStatus() {
        try {
            int updatedCount = postService.closeExpiredPosts();
            log.info("만료 게시글 상태 업데이트됨 - count={}", updatedCount);
        } catch (Exception e) {
            log.error("만료 게시글 상태 업데이트 스케줄러 실패", e);
        }
    }
}
