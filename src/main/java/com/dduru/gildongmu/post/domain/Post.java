package com.dduru.gildongmu.post.domain;

import com.dduru.gildongmu.auth.domain.User;
import com.dduru.gildongmu.auth.enums.AgeGroup;
import com.dduru.gildongmu.auth.enums.Gender;
import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.post.enums.Destination;
import com.dduru.gildongmu.post.enums.PostStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id", nullable = false)
    private Destination destination;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "recruit_capacity", nullable = false)
    @ColumnDefault("1")
    private Integer recruitCapacity = 1;

    @Column(name = "recruit_deadline", nullable = false)
    private LocalDate recruitDeadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_gender", nullable = false)
    @ColumnDefault("'U'")
    private Gender preferredGender = Gender.U;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_age", nullable = false)
    @ColumnDefault("'UNKNOWN'")
    private AgeGroup preferredAge = AgeGroup.UNKNOWN;

    @Column(name = "budget_min")
    private Integer budgetMin;

    @Column(name = "budget_max")
    private Integer budgetMax;

    @Column(name = "photo_urls", columnDefinition = "JSON")
    private String photoUrls;

    @Column(columnDefinition = "JSON")
    private String tags;

    @Column(name = "view_count", nullable = false)
    @ColumnDefault("0")
    private Integer viewCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @ColumnDefault("'OPEN'")
    private PostStatus status = PostStatus.OPEN;

    @Builder
    public Post(User user, Destination destination, String title, String content,
                LocalDate startDate, LocalDate endDate, Integer recruitCapacity,
                LocalDate recruitDeadline, Gender preferredGender, AgeGroup preferredAge,
                Integer budgetMin, Integer budgetMax, String photoUrls, String tags) {
        this.user = user;
        this.destination = destination;
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.recruitCapacity = recruitCapacity != null ? recruitCapacity : 1;
        this.recruitDeadline = recruitDeadline;
        this.preferredGender = preferredGender != null ? preferredGender : Gender.U;
        this.preferredAge = preferredAge != null ? preferredAge : AgeGroup.UNKNOWN;
        this.budgetMin = budgetMin;
        this.budgetMax = budgetMax;
        this.photoUrls = photoUrls;
        this.tags = tags;
        this.viewCount = 0;
    }
}
