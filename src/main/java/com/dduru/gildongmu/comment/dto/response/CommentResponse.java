package com.dduru.gildongmu.comment.dto.response;

import com.dduru.gildongmu.comment.domain.Comment;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.service.ProfileImageResolver;

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
    public static CommentResponse from(Comment comment, ProfileImageResolver profileImageResolver) {
        List<CommentResponse> childrenResponses = comment.getChildren().stream()
                .map(child -> from(child, profileImageResolver))
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
            Profile profile = comment.getUser().getProfile();
            content = comment.getContent();
            author = profile.getNickname();
            authorProfileImage = profileImageResolver.resolve(profile);
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
