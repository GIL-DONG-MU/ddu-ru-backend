package com.dduru.gildongmu.survey.repository;

import com.dduru.gildongmu.survey.domain.TravelTendency;
import com.dduru.gildongmu.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TravelTendencyRepository extends JpaRepository<TravelTendency, Long> {
    Optional<TravelTendency> findByUser(User user);
}
