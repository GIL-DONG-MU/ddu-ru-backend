package com.dduru.gildongmu.report.repository;

import com.dduru.gildongmu.report.domain.Report;
import com.dduru.gildongmu.report.domain.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    @Query(
            value = """
                    SELECT r FROM Report r
                    LEFT JOIN FETCH r.post
                    LEFT JOIN FETCH r.user
                    LEFT JOIN FETCH r.reviewer
                    WHERE (:status IS NULL OR r.status = :status)
                    """,
            countQuery = """
                    SELECT COUNT(r) FROM Report r
                    WHERE (:status IS NULL OR r.status = :status)
                    """
    )
    Page<Report> findAllByStatusOptional(@Param("status") ReportStatus status, Pageable pageable);
}
