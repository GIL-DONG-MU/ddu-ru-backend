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
        log.info("모집 마감일 지난 게시글 상태 업데이트 스케줄러 시작");
        
        try {
            int updatedCount = postService.closeExpiredPosts();
            log.info("모집 마감일 지난 게시글 {}개 CLOSED 상태로 변경 완료", updatedCount);
        } catch (Exception e) {
            log.error("게시글 상태 업데이트 스케줄러 실행 중 오류 발생", e);
        }
    }
}
