package com.dduru.gildongmu.comment.repository;

import com.dduru.gildongmu.comment.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
