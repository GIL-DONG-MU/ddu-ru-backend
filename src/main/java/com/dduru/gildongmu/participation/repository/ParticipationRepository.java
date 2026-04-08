package com.dduru.gildongmu.participation.repository;

import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.exception.ParticipationNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipationRepository extends JpaRepository<Participation, Long>, ParticipationRepositoryCustom {

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    default Participation getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(ParticipationNotFoundException::new);
    }
}
