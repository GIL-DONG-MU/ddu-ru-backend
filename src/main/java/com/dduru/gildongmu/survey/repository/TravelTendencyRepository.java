package com.dduru.gildongmu.survey.repository;

import com.dduru.gildongmu.survey.domain.TravelTendency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TravelTendencyRepository extends JpaRepository<TravelTendency, Long> {
    Optional<TravelTendency> findByUser_Id(Long userId);
}
