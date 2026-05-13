package com.dduru.gildongmu.journey.repository;

import com.dduru.gildongmu.journey.domain.JourneyPost;
import com.dduru.gildongmu.journey.exception.JourneyPostNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JourneyPostRepository extends JpaRepository<JourneyPost, Long> {

    @Query("""
            SELECT jp
            FROM JourneyPost jp
            JOIN FETCH jp.author author
            LEFT JOIN FETCH author.profile profile
            LEFT JOIN FETCH profile.avatar
            LEFT JOIN FETCH profile.bgColor
            WHERE jp.journey.id = :journeyId
              AND jp.isDeleted = false
              AND (
                    :cursorId IS NULL
                    OR (
                        :cursorNotice = true
                        AND jp.isNotice = true
                        AND (
                            jp.createdAt < :cursorCreatedAt
                            OR (jp.createdAt = :cursorCreatedAt AND jp.id < :cursorId)
                        )
                    )
                    OR (
                        :cursorNotice = true
                        AND jp.isNotice = false
                    )
                    OR (
                        :cursorNotice = false
                        AND jp.isNotice = false
                        AND (
                            jp.createdAt < :cursorCreatedAt
                            OR (jp.createdAt = :cursorCreatedAt AND jp.id < :cursorId)
                        )
                    )
              )
            ORDER BY jp.isNotice DESC,
                     jp.createdAt DESC,
                     jp.id DESC
            """)
    List<JourneyPost> findActivePostsByJourneyIdWithAuthorProfile(
            @Param("journeyId") Long journeyId,
            @Param("cursorNotice") Boolean cursorNotice,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            SELECT jp
            FROM JourneyPost jp
            JOIN FETCH jp.journey
            JOIN FETCH jp.author author
            LEFT JOIN FETCH author.profile profile
            LEFT JOIN FETCH profile.avatar
            LEFT JOIN FETCH profile.bgColor
            WHERE jp.id = :journeyPostId
              AND jp.journey.id = :journeyId
              AND jp.isDeleted = false
            """)
    Optional<JourneyPost> findActivePostByIdAndJourneyIdWithAuthorProfile(
            @Param("journeyPostId") Long journeyPostId,
            @Param("journeyId") Long journeyId
    );

    default JourneyPost getActivePostByIdAndJourneyIdOrThrow(Long journeyPostId, Long journeyId) {
        return findActivePostByIdAndJourneyIdWithAuthorProfile(journeyPostId, journeyId)
                .orElseThrow(JourneyPostNotFoundException::new);
    }
}
