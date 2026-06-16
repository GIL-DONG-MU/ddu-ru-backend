package com.dduru.gildongmu.superhost.repository;

import com.dduru.gildongmu.superhost.domain.SuperHostExposure;
import com.dduru.gildongmu.superhost.domain.enums.SuperHostExposureStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SuperHostExposureRepository extends JpaRepository<SuperHostExposure, Long> {
    boolean existsByUser_IdAndStatusAndEndedAtAfter(Long userId, SuperHostExposureStatus status, LocalDateTime now);

    boolean existsByPost_IdAndStatusAndEndedAtAfter(Long postId, SuperHostExposureStatus status, LocalDateTime now);

    @Query("SELECT e.post.id, e.endedAt FROM SuperHostExposure e WHERE e.post.id IN :postIds AND e.status = :status AND e.endedAt > :now")
    List<Object[]> findActivePostIdsWithEndedAt(@Param("postIds") Collection<Long> postIds,
                                                @Param("status") SuperHostExposureStatus status,
                                                @Param("now") LocalDateTime now);

    @EntityGraph(attributePaths = "post")
    Optional<SuperHostExposure> findFirstByUser_IdAndStatusAndEndedAtAfterOrderByStartedAtDesc(
            Long userId, SuperHostExposureStatus status, LocalDateTime now
    );

    @Query("""
            SELECT e
            FROM SuperHostExposure e
            JOIN FETCH e.post p
            JOIN FETCH p.destination
            JOIN FETCH p.user u
            LEFT JOIN FETCH u.profile
            WHERE e.status = :status
              AND e.endedAt > :now
              AND p.isDeleted = false
              AND p.status = com.dduru.gildongmu.post.domain.enums.PostStatus.OPEN
            ORDER BY e.startedAt DESC, p.likeCount DESC
            """)
    List<SuperHostExposure> findVisibleExposures(@Param("status") SuperHostExposureStatus status,
                                                 @Param("now") LocalDateTime now,
                                                 Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE SuperHostExposure e
            SET e.status = :endedStatus
            WHERE e.status = :activeStatus
              AND e.endedAt <= :now
            """)
    int endExpiredExposures(@Param("activeStatus") SuperHostExposureStatus activeStatus,
                            @Param("endedStatus") SuperHostExposureStatus endedStatus,
                            @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE SuperHostExposure e
            SET e.status = :cancelledStatus,
                e.endedAt = :now
            WHERE e.post.id = :postId
              AND e.status = :activeStatus
              AND e.endedAt > :now
            """)
    int cancelActiveExposureByPostId(@Param("postId") Long postId,
                                     @Param("activeStatus") SuperHostExposureStatus activeStatus,
                                     @Param("cancelledStatus") SuperHostExposureStatus cancelledStatus,
                                     @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE SuperHostExposure e
            SET e.status = :cancelledStatus,
                e.endedAt = :now
            WHERE e.status = :activeStatus
              AND e.endedAt > :now
              AND e.post.status = com.dduru.gildongmu.post.domain.enums.PostStatus.CLOSED
            """)
    int cancelActiveExposureByClosedPosts(@Param("activeStatus") SuperHostExposureStatus activeStatus,
                                          @Param("cancelledStatus") SuperHostExposureStatus cancelledStatus,
                                          @Param("now") LocalDateTime now);
}
