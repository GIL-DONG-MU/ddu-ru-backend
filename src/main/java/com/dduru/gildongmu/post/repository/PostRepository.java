package com.dduru.gildongmu.post.repository;

import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.PostStatus;
import com.dduru.gildongmu.post.exception.PostNotFoundException;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
    @Query("SELECT p FROM Post p WHERE p.id = :id AND p.isDeleted = false")
    Optional<Post> findActiveById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Post p WHERE p.id = :id AND p.isDeleted = false")
    Optional<Post> findActiveByIdWithLock(@Param("id") Long id);

    @Query("""
            SELECT p
            FROM Post p
            WHERE p.id = :postId
              AND p.user.id = :userId
              AND p.isDeleted = false
              AND p.status = :status
            """)
    Optional<Post> findSuperHostApplicableByIdAndUserId(
            @Param("postId") Long postId,
            @Param("userId") Long userId,
            @Param("status") PostStatus status
    );

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.status = 'CLOSED' WHERE p.status = 'OPEN' AND p.recruitDeadline < :today")
    int closeExpiredPostsByDate(@Param("today") LocalDate today);

    @Query("""
            SELECT p
            FROM Post p
            JOIN FETCH p.destination
            WHERE p.user.id = :userId
              AND p.isDeleted = false
              AND p.endDate >= :today
            ORDER BY p.startDate ASC, p.id DESC
            """)
    List<Post> findActiveJourneyPostsByOwnerId(
            @Param("userId") Long userId,
            @Param("today") LocalDate today
    );

    @Query("""
            SELECT p
            FROM Post p
            JOIN FETCH p.destination
            WHERE p.user.id = :userId
              AND p.isDeleted = false
              AND p.endDate < :today
            ORDER BY p.endDate DESC, p.id DESC
            """)
    List<Post> findCompletedJourneyPostsByOwnerId(
            @Param("userId") Long userId,
            @Param("today") LocalDate today
    );

    default Post getActiveByIdOrThrow(Long id) {
        return findActiveById(id)
                .orElseThrow(PostNotFoundException::new);
    }

    default Post getActiveByIdWithLockOrThrow(Long id) {
        return findActiveByIdWithLock(id)
                .orElseThrow(PostNotFoundException::new);
    }

    default Post getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(PostNotFoundException::new);
    }
}
