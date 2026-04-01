package com.dduru.gildongmu.participation.repository;

import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import com.dduru.gildongmu.participation.dto.response.ParticipationRetrieveResponse;
import com.dduru.gildongmu.participation.exception.ParticipationNotFoundException;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    @EntityGraph(attributePaths = "user")
    List<Participation> findByPostIdOrderByCreatedAtDesc(Long postId);

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    Optional<Participation> findByPostIdAndUserId(Long postId, Long userId);

    List<Participation> findByPostIdAndStatusOrderByCreatedAtAsc(Long postId, ParticipationStatus status);

    default Participation getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(ParticipationNotFoundException::new);
    }

    @Query("""
            SELECT new com.dduru.gildongmu.participation.dto.response.ParticipationRetrieveResponse(
                p.id, u.id, u.name, p.message, p.status, p.createdAt, p.contactedAt, p.approvedAt, p.rejectedAt, p.post.id, p.post.title
            )
            FROM Participation p
            JOIN p.user u
            WHERE p.post.user.id = :userId
              AND p.post.isDeleted = false
            ORDER BY p.createdAt DESC
            """)
    List<ParticipationRetrieveResponse> findAllParticipantByCreatedAtDesc(@Param(value = "userId") Long userId);

    @Query("""
            SELECT new com.dduru.gildongmu.participation.dto.response.ParticipationRetrieveResponse(
                p.id, u.id, u.name, p.message, p.status, p.createdAt, p.contactedAt, p.approvedAt, p.rejectedAt, p.post.id, p.post.title
            )
            FROM Participation p
            JOIN p.user u
            WHERE p.post.user.id = :userId
              AND p.post.isDeleted = false
              AND p.status = :status
            ORDER BY p.createdAt DESC
            """)
    List<ParticipationRetrieveResponse> findAllParticipantByStatusAndCreatedAtDesc(
            @Param(value = "userId") Long userId,
            @Param(value = "status") ParticipationStatus status
    );
}
