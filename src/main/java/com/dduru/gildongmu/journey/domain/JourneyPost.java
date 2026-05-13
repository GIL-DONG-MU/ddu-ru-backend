package com.dduru.gildongmu.journey.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.journey.exception.InvalidJourneyPostException;
import com.dduru.gildongmu.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Entity
@Table(name = "journey_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JourneyPost extends BaseTimeEntity {
    private static final int CONTENT_MAX_LENGTH = 300;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_id", nullable = false)
    private Journey journey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id", nullable = false)
    private User author;

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
            String content,
            String imageUrl
    ) {
        this.journey = journey;
        this.author = author;
        this.content = validateContent(content);
        this.imageUrl = imageUrl;
        this.isNotice = false;
        this.isDeleted = false;
    }

    public static JourneyPost create(
            Journey journey,
            User author,
            String content,
            String imageUrl
    ) {
        return JourneyPost.builder()
                .journey(journey)
                .author(author)
                .content(content)
                .imageUrl(imageUrl)
                .build();
    }

    public void update(String content, boolean applyImageUrlPatch, String imageUrl) {
        if (content != null) {
            updateContent(content);
        }
        if (applyImageUrlPatch) {
            this.imageUrl = imageUrl;
        }
    }

    private void updateContent(String content) {
        this.content = validateContent(content);
    }

    public void delete(Long deletedBy, LocalDateTime deletedAt) {
        this.isDeleted = true;
        this.deletedBy = deletedBy;
        this.deletedAt = deletedAt;
    }

    public boolean isAuthor(Long userId) {
        return author.getId().equals(userId);
    }

    private static String validateContent(String content) {
        if (!StringUtils.hasText(content)) {
            throw InvalidJourneyPostException.invalidContent();
        }

        int length = content.codePointCount(0, content.length());
        if (length > CONTENT_MAX_LENGTH) {
            throw InvalidJourneyPostException.invalidContent();
        }
        return content;
    }
}
