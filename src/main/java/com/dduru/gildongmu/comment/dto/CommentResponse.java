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
        int likeCount,
        List<CommentResponse> children
) {
    public static CommentResponse from(Comment comment) {
        List<CommentResponse> childrenResponses = comment.getChildren().stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());

        String content;
        String author;
        String authorProfileImage;

        if (comment.isDeleted()) {
            content = "작성자에 의해 삭제된 댓글입니다.";
            author = "알 수 없음";
            authorProfileImage = null;
        } else if (comment.getUser() == null) {
            content = "작성자 정보가 없습니다.";
            author = "알 수 없음";
            authorProfileImage = null;
        } else {
            content = comment.getContent();
            author = comment.getUser().getProfile().getNickname();
            authorProfileImage = comment.getUser().getProfile().getUploadedImageUrl();
        }

        return new CommentResponse(
                comment.getId(),
                content,
                author,
                authorProfileImage,
                comment.getCreatedAt(),
                comment.getLikeCount(),
                childrenResponses
        );
    }
}
