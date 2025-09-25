package com.dduru.gildongmu.comment.dto;

import com.dduru.gildongmu.comment.domain.Comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record CommentResponse(
        Long id,
        String content,
        String author,
        String authorProfileImage,
        LocalDateTime createdAt,
        List<CommentResponse> children
) {
    public static CommentResponse from(Comment comment) {
        List<CommentResponse> childrenResponses = comment.getChildren().stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());

        String content;
        if (comment.isDeleted()) {
            content = "작성자에 의해 삭제된 댓글입니다.";
        } else {
            content = comment.getContent();
        }

        return new CommentResponse(
                comment.getId(),
                content,
                comment.getUser().getNickname(),
                comment.getUser().getProfileImage(),
                comment.getCreatedAt(),
                childrenResponses
        );
    }
}
