package com.dduru.gildongmu.participation.repository;

import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.exception.ParticipationNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    
    List<Participation> findByPostIdOrderByCreatedAtAsc(Long postId);
    
    @Query("SELECT COUNT(p) FROM Participation p WHERE p.post.id = :postId AND p.status = 'APPROVED'")
    int countApprovedParticipationsByPostId(@Param("postId") Long postId);
    
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    default Participation getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new ParticipationNotFoundException("참여신청을 찾을 수 없습니다. participationId=" + id));
    }
}
