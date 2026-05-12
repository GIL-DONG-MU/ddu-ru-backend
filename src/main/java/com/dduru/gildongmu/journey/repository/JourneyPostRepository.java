package com.dduru.gildongmu.journey.repository;

import com.dduru.gildongmu.journey.domain.JourneyPost;
import com.dduru.gildongmu.journey.exception.JourneyPostNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
            ORDER BY jp.isNotice DESC,
                     jp.createdAt DESC,
                     jp.id DESC
            """)
    List<JourneyPost> findActivePostsByJourneyIdWithAuthorProfile(
            @Param("journeyId") Long journeyId
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
