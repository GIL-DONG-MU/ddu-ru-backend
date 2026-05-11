package com.dduru.gildongmu.participation.repository;

import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import com.dduru.gildongmu.participation.exception.ParticipationNotFoundException;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long>, ParticipationRepositoryCustom {

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    @Query("""
            SELECT p
            FROM Participation p
            JOIN FETCH p.post po
            JOIN FETCH po.user
            WHERE p.user.id = :userId
              AND po.isDeleted = false
            ORDER BY p.createdAt DESC
            """)
    List<Participation> findMyApplicationsForVisiblePosts(@Param("userId") Long userId);

    Optional<Participation> findByPostIdAndUserId(Long postId, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Participation> findByPostIdAndUserIdAndStatus(Long postId, Long userId, ParticipationStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT p
            FROM Participation p
            JOIN FETCH p.post
            JOIN FETCH p.user
            WHERE p.id = :id
            """)
    Optional<Participation> findByIdWithLock(@Param("id") Long id);

    default Participation getByIdWithLockOrThrow(Long id) {
        return findByIdWithLock(id)
                .orElseThrow(ParticipationNotFoundException::new);
    }
}
