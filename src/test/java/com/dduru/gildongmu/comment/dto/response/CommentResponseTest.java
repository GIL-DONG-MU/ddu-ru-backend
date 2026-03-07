package com.dduru.gildongmu.comment.dto.response;

import com.dduru.gildongmu.comment.domain.Comment;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.profile.service.ProfileImageResolver;
import com.dduru.gildongmu.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("CommentResponse 테스트")
class CommentResponseTest {

    @Test
    @DisplayName("DEFAULT 타입이면 댓글 작성자 프로필 이미지는 기본 URL을 사용한다")
    void from_DEFAULT_usesDefaultImageUrl() {
        // given
        Comment comment = mock(Comment.class);
        User user = mock(User.class);
        Profile profile = mock(Profile.class);
        ProfileImageResolver profileImageResolver = mock(ProfileImageResolver.class);
        LocalDateTime now = LocalDateTime.now();

        when(comment.getChildren()).thenReturn(Collections.emptyList());
        when(comment.isDeleted()).thenReturn(false);
        when(comment.getUser()).thenReturn(user);
        when(comment.getContent()).thenReturn("댓글 내용");
        when(comment.getId()).thenReturn(1L);
        when(comment.getCreatedAt()).thenReturn(now);
        when(comment.getLikeCount()).thenReturn(3);

        when(user.getProfile()).thenReturn(profile);
        when(profile.getNickname()).thenReturn("작성자");
        when(profile.getProfileImageType()).thenReturn(ProfileImageType.DEFAULT);
        when(profileImageResolver.resolve(profile)).thenReturn("https://example.com/default.png");

        // when
        CommentResponse response = CommentResponse.from(comment, profileImageResolver);

        // then
        assertThat(response.author()).isEqualTo("작성자");
        assertThat(response.authorProfileImage()).isEqualTo("https://example.com/default.png");
    }

    @Test
    @DisplayName("UPLOADED 타입이면 댓글 작성자 프로필 이미지는 저장된 URL을 사용한다")
    void from_UPLOADED_usesUploadedImageUrl() {
        // given
        Comment comment = mock(Comment.class);
        User user = mock(User.class);
        Profile profile = mock(Profile.class);
        ProfileImageResolver profileImageResolver = mock(ProfileImageResolver.class);
        LocalDateTime now = LocalDateTime.now();

        when(comment.getChildren()).thenReturn(Collections.emptyList());
        when(comment.isDeleted()).thenReturn(false);
        when(comment.getUser()).thenReturn(user);
        when(comment.getContent()).thenReturn("댓글 내용");
        when(comment.getId()).thenReturn(2L);
        when(comment.getCreatedAt()).thenReturn(now);
        when(comment.getLikeCount()).thenReturn(1);

        when(user.getProfile()).thenReturn(profile);
        when(profile.getNickname()).thenReturn("작성자2");
        when(profile.getProfileImageType()).thenReturn(ProfileImageType.UPLOADED);
        when(profile.getUploadedImageUrl()).thenReturn("https://example.com/uploaded.png");
        when(profileImageResolver.resolve(profile)).thenReturn("https://example.com/uploaded.png");

        // when
        CommentResponse response = CommentResponse.from(comment, profileImageResolver);

        // then
        assertThat(response.author()).isEqualTo("작성자2");
        assertThat(response.authorProfileImage()).isEqualTo("https://example.com/uploaded.png");
    }
}
