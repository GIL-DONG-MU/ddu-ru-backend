package com.dduru.gildongmu.journey.repository;

import com.dduru.gildongmu.journey.domain.JourneyPostImage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface JourneyPostImageRepository extends JpaRepository<JourneyPostImage, Long> {

    @Query("""
            SELECT img
            FROM JourneyPostImage img
            JOIN FETCH img.journeyPost post
            WHERE post.journey.id = :journeyId
              AND post.isDeleted = false
              AND (
                    :cursorPostId IS NULL
                    OR post.createdAt < :cursorCreatedAt
                    OR (post.createdAt = :cursorCreatedAt AND post.id < :cursorPostId)
                    OR (post.createdAt = :cursorCreatedAt AND post.id = :cursorPostId AND img.sortOrder > :cursorSortOrder)
                  )
            ORDER BY post.createdAt DESC, post.id DESC, img.sortOrder ASC
            """)
    List<JourneyPostImage> findGalleryImages(
            @Param("journeyId") Long journeyId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorPostId") Long cursorPostId,
            @Param("cursorSortOrder") Integer cursorSortOrder,
            Pageable pageable
    );
}
