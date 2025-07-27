package com.dduru.gildongmu.post.repository;

import com.dduru.gildongmu.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
