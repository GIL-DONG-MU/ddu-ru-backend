package com.dduru.gildongmu.journey.repository;

import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import com.dduru.gildongmu.journey.exception.JourneyNotFoundException;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JourneyRepository extends JpaRepository<Journey, Long> {

    Optional<Journey> findByPostId(Long postId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT j
            FROM Journey j
            WHERE j.id = :journeyId
            """)
    Optional<Journey> findByIdWithLock(@Param("journeyId") Long journeyId);

    @Query("""
            SELECT j
            FROM JourneyMember jm
            JOIN jm.journey j
            WHERE j.id = :journeyId
              AND jm.user.id = :userId
              AND jm.status = :status
            """)
    Optional<Journey> findUpdatableJourneyByIdAndUserId(
            @Param("journeyId") Long journeyId,
            @Param("userId") Long userId,
            @Param("status") JourneyMemberStatus status
    );

    @Query("""
            SELECT j.post.id
            FROM Journey j
            WHERE j.id = :journeyId
            """)
    Optional<Long> findPostIdById(@Param("journeyId") Long journeyId);

    @Query("""
            SELECT j
            FROM Journey j
            JOIN FETCH j.post p
            JOIN FETCH p.destination
            WHERE j.id = :journeyId
            """)
    Optional<Journey> findByIdWithPostContext(@Param("journeyId") Long journeyId);

    default Journey getByIdOrThrow(Long journeyId) {
        return findById(journeyId).orElseThrow(JourneyNotFoundException::new);
    }

    default Journey getByIdWithLockOrThrow(Long journeyId) {
        return findByIdWithLock(journeyId).orElseThrow(JourneyNotFoundException::new);
    }

    default Journey getByPostIdOrThrow(Long postId) {
        return findByPostId(postId).orElseThrow(JourneyNotFoundException::new);
    }

    default Long getPostIdByIdOrThrow(Long journeyId) {
        return findPostIdById(journeyId)
                .orElseThrow(JourneyNotFoundException::new);
    }

    default Journey getByIdWithPostContextOrThrow(Long journeyId) {
        return findByIdWithPostContext(journeyId)
                .orElseThrow(JourneyNotFoundException::new);
    }
}
