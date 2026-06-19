package com.dduru.gildongmu.journey.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "journey_post_images",
        indexes = @Index(name = "idx_journey_post_images_post", columnList = "journey_post_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JourneyPostImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_post_id", nullable = false)
    private JourneyPost journeyPost;

    @Column(name = "image_url", columnDefinition = "TEXT", nullable = false)
    private String imageUrl;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    private JourneyPostImage(JourneyPost journeyPost, String imageUrl, int sortOrder) {
        this.journeyPost = journeyPost;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }

    public static JourneyPostImage of(JourneyPost journeyPost, String imageUrl, int sortOrder) {
        return new JourneyPostImage(journeyPost, imageUrl, sortOrder);
    }
}
