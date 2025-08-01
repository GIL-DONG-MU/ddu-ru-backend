package com.dduru.gildongmu.post.repository;

import com.dduru.gildongmu.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p WHERE p.id = :id AND p.isDeleted = false")
    Optional<Post> findActiveById(@Param("id") Long id);
}
