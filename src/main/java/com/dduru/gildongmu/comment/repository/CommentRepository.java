package com.dduru.gildongmu.comment.repository;

import com.dduru.gildongmu.comment.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {
    Optional<Comment> findByIdAndDeletedFalse(Long id);
}
