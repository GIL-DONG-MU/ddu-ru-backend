package com.dduru.gildongmu.journey.repository;

import com.dduru.gildongmu.journey.domain.JourneySchedule;
import com.dduru.gildongmu.journey.exception.JourneyScheduleNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JourneyScheduleRepository extends JpaRepository<JourneySchedule, Long> {

    @Query("""
            SELECT js
            FROM JourneySchedule js
            WHERE js.journey.id = :journeyId
              AND js.isDeleted = false
            ORDER BY js.dayOffset ASC, js.startTime ASC NULLS LAST, js.id ASC
            """)
    List<JourneySchedule> findActiveSchedulesByJourneyId(@Param("journeyId") Long journeyId);

    @Query("""
            SELECT js
            FROM JourneySchedule js
            WHERE js.id = :scheduleId
              AND js.journey.id = :journeyId
              AND js.isDeleted = false
            """)
    Optional<JourneySchedule> findActiveByIdAndJourneyId(
            @Param("scheduleId") Long scheduleId,
            @Param("journeyId") Long journeyId
    );

    default JourneySchedule getActiveByIdAndJourneyIdOrThrow(Long scheduleId, Long journeyId) {
        return findActiveByIdAndJourneyId(scheduleId, journeyId)
                .orElseThrow(JourneyScheduleNotFoundException::new);
    }

    @Query("""
            SELECT js
            FROM JourneySchedule js
            WHERE js.journey.id = :journeyId
              AND js.isDeleted = false
              AND js.dayOffset >= :dayOffset
            """)
    List<JourneySchedule> findActiveSchedulesWithDayOffsetGreaterThanOrEqual(
            @Param("journeyId") Long journeyId,
            @Param("dayOffset") int dayOffset
    );

    int countByJourneyIdAndIsDeletedFalse(Long journeyId);
}
