package com.dduru.gildongmu.destination.repository;

import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.post.exception.DestinationNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DestinationRepository extends JpaRepository<Destination, Long> {
    default Destination getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> DestinationNotFoundException.of(id));
    }
}
