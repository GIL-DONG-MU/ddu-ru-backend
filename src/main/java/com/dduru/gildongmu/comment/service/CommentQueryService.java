package com.dduru.gildongmu.comment.service;

import com.dduru.gildongmu.comment.domain.Comment;
import com.dduru.gildongmu.comment.dto.CommentResponse;
import com.dduru.gildongmu.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService {

    private final CommentRepository commentRepository;

    public List<CommentResponse> retrieve(Long postId) {
        List<Comment> comments = commentRepository.findCommentsByPostId(postId);

        Map<Long, Comment> commentMap = comments.stream()
                .collect(Collectors.toMap(Comment::getId, comment -> comment));

        List<Comment> rootComments = new ArrayList<>();
        for (Comment comment : comments) {
            if (comment.getParent() != null) {
                Comment parent = commentMap.get(comment.getParent().getId());
                if (parent != null) {
                    parent.getChildren().add(comment);
                }
            } else {
                rootComments.add(comment);
            }
        }

        return rootComments.stream()
                .filter(comment -> !comment.isDeleted() || !comment.getChildren().isEmpty())
                .map(CommentResponse::from)
                .toList();
    }
}
