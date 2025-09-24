package com.dduru.gildongmu.comment.repository;

import com.dduru.gildongmu.comment.domain.Comment;

import java.util.List;

public interface CommentRepositoryCustom {
    List<Comment> findCommentsByPostId(Long postId);
}
