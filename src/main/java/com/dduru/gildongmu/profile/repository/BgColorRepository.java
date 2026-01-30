package com.dduru.gildongmu.profile.repository;

import com.dduru.gildongmu.profile.domain.BgColor;
import com.dduru.gildongmu.profile.exception.BgColorNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BgColorRepository extends JpaRepository<BgColor, Long> {

    List<BgColor> findAllByOrderByDisplayOrderAsc();

    default BgColor getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> BgColorNotFoundException.of(id));
    }
}
