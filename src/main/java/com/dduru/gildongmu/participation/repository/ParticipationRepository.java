package com.dduru.gildongmu.participation.repository;

import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import com.dduru.gildongmu.participation.exception.ParticipationNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    List<Participation> findByPostIdOrderByCreatedAtAsc(Long postId);

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    Optional<Participation> findByPostIdAndUserId(Long postId, Long userId);

    List<Participation> findByPostIdAndStatusOrderByCreatedAtAsc(Long postId, ParticipationStatus status);

    default Participation getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(ParticipationNotFoundException::new);
    }
}
