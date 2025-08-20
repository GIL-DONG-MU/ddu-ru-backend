package com.dduru.gildongmu.participation.repository;

import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.exception.ParticipationNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    
    List<Participation> findByPostIdOrderByCreatedAtAsc(Long postId);
    
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    default Participation getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> ParticipationNotFoundException.of(id));
    }
}
