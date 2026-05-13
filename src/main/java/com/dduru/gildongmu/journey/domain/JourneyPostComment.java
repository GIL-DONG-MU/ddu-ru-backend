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
@Table(name = "journey_post_comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JourneyPostComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_post_id", nullable = false)
    private JourneyPost journeyPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 300)
    private String content;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Builder(access = AccessLevel.PRIVATE)
    private JourneyPostComment(JourneyPost journeyPost, User author, String content) {
        this.journeyPost = journeyPost;
        this.author = author;
        this.content = content;
        this.isDeleted = false;
    }

    public static JourneyPostComment create(JourneyPost journeyPost, User author, String content) {
        return JourneyPostComment.builder()
                .journeyPost(journeyPost)
                .author(author)
                .content(content)
                .build();
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void softDelete(Long deletedBy, LocalDateTime deletedAt) {
        this.isDeleted = true;
        this.deletedBy = deletedBy;
        this.deletedAt = deletedAt;
    }

    public boolean isAuthor(Long userId) {
        return author.getId().equals(userId);
    }
}
