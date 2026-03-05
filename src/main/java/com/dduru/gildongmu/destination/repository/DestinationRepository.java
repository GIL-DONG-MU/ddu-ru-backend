package com.dduru.gildongmu.destination.repository;

import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.destination.exception.DestinationNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DestinationRepository extends JpaRepository<Destination, Long>, DestinationRepositoryCustom {

    List<Destination> findByCityIn(List<String> cities);

    default Destination getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> DestinationNotFoundException.of(id));
    }
}
