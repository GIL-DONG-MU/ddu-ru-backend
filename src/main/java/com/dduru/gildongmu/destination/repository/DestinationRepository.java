package com.dduru.gildongmu.destination.repository;

import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.destination.exception.DestinationNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface DestinationRepository extends JpaRepository<Destination, Long>, DestinationRepositoryCustom {

    List<Destination> findByCityIn(List<String> cities);

    List<Destination> findByCountryCodeIn(Collection<String> countryCodes);

    @Query(value = "SELECT p.destination_id FROM posts p " +
            "WHERE p.created_at >= :since AND p.is_deleted = false " +
            "GROUP BY p.destination_id " +
            "ORDER BY (COUNT(*) + COALESCE(SUM(p.view_count), 0) + COALESCE(SUM(p.like_count), 0)) DESC " +
            "LIMIT 10", nativeQuery = true)
    List<Long> findPopularDestinationIds(@Param("since") LocalDateTime since);

    default Destination getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(DestinationNotFoundException::new);
    }
}
