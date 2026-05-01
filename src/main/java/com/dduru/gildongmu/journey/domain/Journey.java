package com.dduru.gildongmu.journey.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.post.domain.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "journeys")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Journey extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, unique = true)
    private Post post;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "photo_url", columnDefinition = "TEXT")
    private String photoUrl;

    @Builder(access = AccessLevel.PRIVATE)
    private Journey(
            Post post,
            String title,
            String photoUrl
    ) {
        this.post = post;
        this.title = title;
        this.photoUrl = photoUrl;
    }

    public static Journey create(Post post) {
        return Journey.builder()
                .post(post)
                .title(post.getTitle())
                .photoUrl(post.getPhotoUrl())
                .build();
    }
}
