package com.dduru.gildongmu.like.domain;

import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_likes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "post_like_unique",
                        columnNames = {"user_id", "post_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Builder
    public PostLike(User user, Post post) {
        this.user = user;
        this.post = post;
    }

    public static PostLike createPostLike(User user, Post post) {
        return PostLike.builder()
                .user(user)
                .post(post)
                .build();
    }
}
