package com.dduru.gildongmu.journey.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "journey_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JourneyPost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_id", nullable = false)
    private Journey journey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 30)
    private String title;

    @Column(nullable = false, length = 300)
    private String content;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "is_notice", nullable = false)
    private boolean isNotice;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Builder(access = AccessLevel.PRIVATE)
    private JourneyPost(
            Journey journey,
            User author,
            String title,
            String content,
            String imageUrl
    ) {
        this.journey = journey;
        this.author = author;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.isNotice = false;
        this.isDeleted = false;
    }

    public static JourneyPost create(
            Journey journey,
            User author,
            String title,
            String content,
            String imageUrl
    ) {
        return JourneyPost.builder()
                .journey(journey)
                .author(author)
                .title(title)
                .content(content)
                .imageUrl(imageUrl)
                .build();
    }

    public void update(String title, String content, boolean applyImageUrlPatch, String imageUrl) {
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
        if (applyImageUrlPatch) {
            this.imageUrl = imageUrl;
        }
    }

    public void delete(Long deletedBy, LocalDateTime deletedAt) {
        this.isDeleted = true;
        this.deletedBy = deletedBy;
        this.deletedAt = deletedAt;
    }

    public boolean isAuthor(Long userId) {
        return author.getId().equals(userId);
    }
}
