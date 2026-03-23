package com.dduru.gildongmu.report.repository;

import com.dduru.gildongmu.report.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByPostIdAndUserId(Long postId, Long userId);
}
