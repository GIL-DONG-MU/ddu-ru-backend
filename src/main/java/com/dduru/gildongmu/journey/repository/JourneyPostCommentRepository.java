package com.dduru.gildongmu.journey.repository;

import com.dduru.gildongmu.journey.domain.JourneyPostComment;
import com.dduru.gildongmu.journey.dto.query.JourneyPostCommentCountQueryResult;
import com.dduru.gildongmu.journey.exception.JourneyPostCommentNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface JourneyPostCommentRepository extends JpaRepository<JourneyPostComment, Long> {

    long countByJourneyPost_IdAndIsDeletedFalse(Long journeyPostId);

    // 각 게시글에서 자신보다 최신인 댓글 수가 previewLimit 미만인 댓글만 골라 게시글별 최신 N개를 만든다.
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
            SELECT new com.dduru.gildongmu.journey.dto.query.JourneyPostCommentCountQueryResult(
                c.journeyPost.id,
                COUNT(c)
            )
            FROM JourneyPostComment c
            WHERE c.journeyPost.id IN :journeyPostIds
              AND c.isDeleted = false
            GROUP BY c.journeyPost.id
            """)
    List<JourneyPostCommentCountQueryResult> findCommentCountsByJourneyPostIds(
            @Param("journeyPostIds") Collection<Long> journeyPostIds
    );

    @Query("""
            SELECT c
            FROM JourneyPostComment c
            JOIN FETCH c.journeyPost jp
            JOIN FETCH c.author author
            LEFT JOIN FETCH author.profile profile
            LEFT JOIN FETCH profile.avatar
            LEFT JOIN FETCH profile.bgColor
            WHERE jp.id IN :journeyPostIds
              AND c.isDeleted = false
              AND (
                  SELECT COUNT(c2)
                  FROM JourneyPostComment c2
                  WHERE c2.journeyPost.id = jp.id
                    AND c2.isDeleted = false
                    AND (
                        c2.createdAt > c.createdAt
                        OR (c2.createdAt = c.createdAt AND c2.id > c.id)
                    )
              ) < :previewLimit
            ORDER BY jp.id ASC,
                     c.createdAt DESC,
                     c.id DESC
            """)
    List<JourneyPostComment> findLatestPreviewCommentsByJourneyPostIdsWithAuthorProfile(
            @Param("journeyPostIds") Collection<Long> journeyPostIds,
            @Param("previewLimit") long previewLimit
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
