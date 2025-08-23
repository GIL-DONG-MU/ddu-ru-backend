package com.dduru.gildongmu.post.repository;

import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.exception.PostNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>,PostRepositoryCustom {
    @Query("SELECT p FROM Post p WHERE p.id = :id AND p.isDeleted = false")
    Optional<Post> findActiveById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);

    @Query("SELECT p FROM Post p WHERE p.status = 'OPEN' AND p.recruitDeadline <= :today AND p.isDeleted = false")
    List<Post> findExpiredOpenPosts(@Param("today") LocalDate today);

    default Post getActiveByIdOrThrow(Long id) {
        return findActiveById(id)
                .orElseThrow(() -> PostNotFoundException.of(id));
    }
}
