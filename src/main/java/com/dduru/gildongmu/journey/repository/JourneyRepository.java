package com.dduru.gildongmu.journey.repository;

import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.journey.exception.JourneyNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JourneyRepository extends JpaRepository<Journey, Long> {

    Optional<Journey> findByPostId(Long postId);

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

    default Journey getByPostIdOrThrow(Long postId) {
        return findByPostId(postId).orElseThrow(JourneyNotFoundException::new);
    }

    default Journey getByIdWithPostContextOrThrow(Long journeyId) {
        return findByIdWithPostContext(journeyId)
                .orElseThrow(JourneyNotFoundException::new);
    }
}
