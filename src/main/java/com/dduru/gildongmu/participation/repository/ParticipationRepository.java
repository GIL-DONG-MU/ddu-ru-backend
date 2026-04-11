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
            JOIN FETCH po.destination
            JOIN FETCH po.user
            WHERE p.user.id = :userId
              AND po.isDeleted = false
            ORDER BY p.createdAt DESC
            """)
    List<Participation> findMyApplicationsForVisiblePosts(@Param("userId") Long userId);

    Optional<Participation> findByPostIdAndUserId(Long postId, Long userId);

    @Query("""
            SELECT p
            FROM Participation p
            JOIN FETCH p.user u
            JOIN FETCH u.profile prof
            LEFT JOIN FETCH prof.avatar
            LEFT JOIN FETCH prof.bgColor
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
    Optional<Participation> findByIdForUpdate(@Param("id") Long id);

    default Participation getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(ParticipationNotFoundException::new);
    }

    default Participation getByIdForUpdateOrThrow(Long id) {
        return findByIdForUpdate(id)
                .orElseThrow(ParticipationNotFoundException::new);
    }
}
