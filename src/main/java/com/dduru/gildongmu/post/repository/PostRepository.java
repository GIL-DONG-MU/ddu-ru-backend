package com.dduru.gildongmu.post.repository;

import com.dduru.gildongmu.auth.enums.AgeRange;
import com.dduru.gildongmu.auth.enums.Gender;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p WHERE p.id = :id AND p.isDeleted = false")
    Optional<Post> findActiveById(@Param("id") Long id);

    @Query("""
    SELECT p
    FROM Post p
    WHERE p.isDeleted = false
      AND (:cursor IS NULL OR p.id < :cursor)
      AND (:keyword IS NULL OR p.title LIKE %:keyword% OR p.content LIKE %:keyword%)
      AND (:startDate IS NULL OR p.endDate >= :startDate)
      AND (:endDate IS NULL OR p.startDate <= :endDate)
      AND (:preferredGender IS NULL OR p.preferredGender = :preferredGender)
      AND (:preferredAge IS NULL OR p.preferredAgeMin = :preferredAge OR p.preferredAgeMax = :preferredAge
           OR (:preferredAge >= p.preferredAgeMin AND :preferredAge <= p.preferredAgeMax))
      AND (:destinationId IS NULL OR p.destination.id = :destinationId)
      AND (
            :isRecruiting IS NULL
            OR (
                :isRecruiting = true
                AND p.status = :openStatus
                AND p.recruitDeadline >= CURRENT_DATE
                AND p.recruitCount < p.recruitCapacity
            )
            OR (
                :isRecruiting = false
                AND (
                    p.status = :fullStatus
                    OR p.status = :closedStatus
                    OR p.recruitDeadline < CURRENT_DATE
                    OR p.recruitCount >= p.recruitCapacity
                )
            )
      )
    ORDER BY p.id DESC
""")
    Page<Post> findPostsWithFilters(
            @Param("cursor") Long cursor,
            @Param("keyword") String keyword,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("preferredGender") Gender preferredGender,
            @Param("preferredAge") AgeRange preferredAge,
            @Param("destinationId") Long destinationId,
            @Param("isRecruiting") Boolean isRecruiting,
            @Param("openStatus") PostStatus openStatus,
            @Param("fullStatus") PostStatus fullStatus,
            @Param("closedStatus") PostStatus closedStatus,
            Pageable pageable
    );

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);
}
