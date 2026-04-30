package com.dduru.gildongmu.participation.repository;

import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import com.dduru.gildongmu.participation.exception.ParticipationNotFoundException;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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

    @Query("""
            SELECT p
            FROM Participation p
            JOIN FETCH p.post po
            JOIN FETCH po.destination
            WHERE p.user.id = :userId
              AND p.status = :status
              AND po.isDeleted = false
              AND po.endDate >= :today
            ORDER BY po.startDate ASC, po.id DESC
            """)
    List<Participation> findActiveJourneyParticipationsByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") ParticipationStatus status,
            @Param("today") LocalDate today
    );

    @Query("""
            SELECT p
            FROM Participation p
            JOIN FETCH p.post po
            JOIN FETCH po.destination
            WHERE p.user.id = :userId
              AND p.status = :status
              AND po.isDeleted = false
              AND po.endDate < :today
            ORDER BY po.endDate DESC, po.id DESC
            """)
    List<Participation> findCompletedJourneyParticipationsByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") ParticipationStatus status,
            @Param("today") LocalDate today
    );

    Optional<Participation> findByPostIdAndUserId(Long postId, Long userId);

    @Query("""
            SELECT p
            FROM Participation p
            JOIN FETCH p.user u
            JOIN FETCH u.profile pr
            LEFT JOIN FETCH pr.avatar
            LEFT JOIN FETCH pr.bgColor
            WHERE p.post.id = :postId
              AND p.status = :status
            ORDER BY p.approvedAt ASC, p.id ASC
            """)
    List<Participation> findByPostIdAndStatusWithMemberProfiles(
            @Param("postId") Long postId,
            @Param("status") ParticipationStatus status
    );

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
