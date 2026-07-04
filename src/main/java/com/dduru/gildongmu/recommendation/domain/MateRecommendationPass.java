package com.dduru.gildongmu.recommendation.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "mate_recommendation_passes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_mate_recommendation_passes_user_post",
                columnNames = {"user_id", "post_id"}
        ),
        indexes = @Index(name = "idx_mate_recommendation_passes_post", columnList = "post_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MateRecommendationPass extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    private MateRecommendationPass(User user, Post post) {
        this.user = user;
        this.post = post;
    }

    public static MateRecommendationPass of(User user, Post post) {
        return new MateRecommendationPass(user, post);
    }
}
