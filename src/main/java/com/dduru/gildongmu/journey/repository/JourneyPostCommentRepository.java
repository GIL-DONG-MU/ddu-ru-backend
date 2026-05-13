package com.dduru.gildongmu.journey.repository;

import com.dduru.gildongmu.journey.domain.JourneyPostComment;
import com.dduru.gildongmu.journey.exception.JourneyPostCommentNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JourneyPostCommentRepository extends JpaRepository<JourneyPostComment, Long> {

    long countByJourneyPost_IdAndIsDeletedFalse(Long journeyPostId);

    @Query("""
            SELECT c
            FROM JourneyPostComment c
            JOIN FETCH c.journeyPost jp
            JOIN FETCH c.author author
            LEFT JOIN FETCH author.profile profile
            LEFT JOIN FETCH profile.avatar
            LEFT JOIN FETCH profile.bgColor
            WHERE jp.id = :journeyPostId
              AND c.isDeleted = false
            ORDER BY c.createdAt DESC,
                     c.id DESC
            """)
    List<JourneyPostComment> findActiveCommentsByJourneyPostIdWithAuthorProfile(
            @Param("journeyPostId") Long journeyPostId,
            Pageable pageable
    );

    @Query("""
            SELECT c
            FROM JourneyPostComment c
            JOIN FETCH c.journeyPost jp
            JOIN FETCH c.author author
            LEFT JOIN FETCH author.profile profile
            LEFT JOIN FETCH profile.avatar
            LEFT JOIN FETCH profile.bgColor
            WHERE c.id = :commentId
              AND jp.id = :journeyPostId
              AND c.isDeleted = false
            """)
    Optional<JourneyPostComment> findActiveCommentByIdAndJourneyPostIdWithAuthorProfile(
            @Param("commentId") Long commentId,
            @Param("journeyPostId") Long journeyPostId
    );

    default JourneyPostComment getActiveCommentByIdAndJourneyPostIdOrThrow(Long commentId, Long journeyPostId) {
        return findActiveCommentByIdAndJourneyPostIdWithAuthorProfile(commentId, journeyPostId)
                .orElseThrow(JourneyPostCommentNotFoundException::new);
    }
}
