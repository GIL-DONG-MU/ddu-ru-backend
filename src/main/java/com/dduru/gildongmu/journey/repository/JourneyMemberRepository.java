package com.dduru.gildongmu.journey.repository;

import com.dduru.gildongmu.journey.domain.JourneyMember;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JourneyMemberRepository extends JpaRepository<JourneyMember, Long> {

    Optional<JourneyMember> findByJourneyIdAndUserId(Long journeyId, Long userId);

    boolean existsByJourneyIdAndUserIdAndStatus(Long journeyId, Long userId, JourneyMemberStatus status);

    @Query("""
            SELECT jm
            FROM JourneyMember jm
            JOIN FETCH jm.journey j
            JOIN FETCH j.post p
            JOIN FETCH p.destination
            WHERE jm.user.id = :userId
              AND jm.status = :status
              AND p.isDeleted = false
              AND p.endDate >= :today
            ORDER BY p.startDate ASC, j.id DESC
            """)
    List<JourneyMember> findActiveJourneyMembersByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") JourneyMemberStatus status,
            @Param("today") LocalDate today
    );

    @Query("""
            SELECT jm
            FROM JourneyMember jm
            JOIN FETCH jm.journey j
            JOIN FETCH j.post p
            JOIN FETCH p.destination
            WHERE jm.user.id = :userId
              AND jm.status = :status
              AND p.isDeleted = false
              AND p.endDate < :today
            ORDER BY p.endDate DESC, j.id DESC
            """)
    List<JourneyMember> findCompletedJourneyMembersByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") JourneyMemberStatus status,
            @Param("today") LocalDate today
    );

    @Query("""
            SELECT jm
            FROM JourneyMember jm
            JOIN FETCH jm.user u
            JOIN FETCH u.profile pr
            LEFT JOIN FETCH pr.avatar
            LEFT JOIN FETCH pr.bgColor
            WHERE jm.journey.post.id = :postId
              AND jm.status = :status
            ORDER BY jm.role ASC,
                     jm.joinedAt ASC,
                     jm.id ASC
            """)
    List<JourneyMember> findByJourneyPostIdAndStatusWithMemberProfiles(
            @Param("postId") Long postId,
            @Param("status") JourneyMemberStatus status
    );
}
