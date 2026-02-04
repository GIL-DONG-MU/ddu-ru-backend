package com.dduru.gildongmu.comment.service;

import com.dduru.gildongmu.comment.domain.Comment;
import com.dduru.gildongmu.comment.dto.response.CommentResponse;
import com.dduru.gildongmu.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService {

    private final CommentRepository commentRepository;

    public List<CommentResponse> retrieve(Long postId) {
        log.debug("댓글 목록 조회 시작 - postId: {}", postId);
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

        List<CommentResponse> result = rootComments.stream()
                .filter(comment -> !comment.isDeleted() || !comment.getChildren().isEmpty())
                .map(CommentResponse::from)
                .toList();

        log.info("댓글 목록 조회 완료 - postId: {}, 댓글 수: {}", postId, result.size());
        return result;
    }
}
