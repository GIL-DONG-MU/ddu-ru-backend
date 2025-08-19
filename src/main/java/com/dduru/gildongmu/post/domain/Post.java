package com.dduru.gildongmu.post.domain;

import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.enums.AgeRange;
import com.dduru.gildongmu.user.enums.Gender;
import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.post.enums.PostStatus;
import com.dduru.gildongmu.post.exception.InvalidRecruitCapacityException;
import com.dduru.gildongmu.post.exception.RecruitCountExceedCapacityException;
import com.dduru.gildongmu.post.exception.RecruitCountBelowZeroException;
import com.dduru.gildongmu.post.exception.TravelAlreadyStartedException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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

    @Column(name = "recruit_count", nullable = false)
    @ColumnDefault("0")
    private Integer recruitCount = 0;

    @Column(name = "recruit_deadline", nullable = false)
    private LocalDate recruitDeadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_gender", nullable = false)
    @ColumnDefault("'U'")
    private Gender preferredGender = Gender.U;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_age_min")
    private AgeRange preferredAgeMin;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_age_max")
    private AgeRange preferredAgeMax;

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

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Builder
    public Post(User user, Destination destination, String title, String content,
                LocalDate startDate, LocalDate endDate, Integer recruitCapacity,
                LocalDate recruitDeadline, Gender preferredGender,AgeRange preferredAgeMin, AgeRange preferredAgeMax,
                Integer budgetMin, Integer budgetMax, String photoUrls, String tags) {
        this.user = user;
        this.destination = destination;
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.recruitCapacity = recruitCapacity != null ? recruitCapacity : 1;
        this.recruitCount = 0;
        this.recruitDeadline = recruitDeadline;
        this.preferredGender = preferredGender != null ? preferredGender : Gender.U;
        this.preferredAgeMin = preferredAgeMin;
        this.preferredAgeMax = preferredAgeMax;
        this.budgetMin = budgetMin;
        this.budgetMax = budgetMax;
        this.photoUrls = photoUrls;
        this.tags = tags;
        this.viewCount = 0;
    }

    public static Post createPost(User user, Destination destination, String title, String content,
                                  LocalDate startDate, LocalDate endDate, Integer recruitCapacity,
                                  LocalDate recruitDeadline, Gender preferredGender,AgeRange preferredAgeMin, AgeRange preferredAgeMax,
                                  Integer budgetMin, Integer budgetMax, String photoUrls, String tags){

        return Post.builder()
                .user(user)
                .destination(destination)
                .title(title)
                .content(content)
                .startDate(startDate)
                .endDate(endDate)
                .recruitCapacity(recruitCapacity)
                .recruitDeadline(recruitDeadline)
                .preferredGender(preferredGender)
                .preferredAgeMin(preferredAgeMin)
                .preferredAgeMax(preferredAgeMax)
                .budgetMin(budgetMin)
                .budgetMax(budgetMax)
                .photoUrls(photoUrls)
                .tags(tags)
                .build();
    }

    public void updatePost(Destination destination, String title, String content,
                           LocalDate startDate, LocalDate endDate, Integer recruitCapacity,
                           LocalDate recruitDeadline, Gender preferredGender,AgeRange preferredAgeMin, AgeRange preferredAgeMax,
                           Integer budgetMin, Integer budgetMax, String photoUrls, String tags){
        validateUpdatePermission();

        this.destination = destination;
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.recruitDeadline = recruitDeadline;
        this.preferredGender = preferredGender != null ? preferredGender : Gender.U;
        this.preferredAgeMin = preferredAgeMin;
        this.preferredAgeMax = preferredAgeMax;
        this.budgetMin = budgetMin;
        this.budgetMax = budgetMax;
        this.photoUrls = photoUrls;
        this.tags = tags;

        updateRecruitCapacity(recruitCapacity);
    }

    public void softDelete(Long userId) {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = userId;
    }


    public boolean isRecruitOpen() {
        return status == PostStatus.OPEN &&
                !isRecruitmentClosed() &&
                recruitCount < recruitCapacity;
    }

    public int getDaysLeftForRecruitment() {
        int daysLeft = (int) ChronoUnit.DAYS.between(LocalDate.now(), recruitDeadline) + 1;
        return Math.max(daysLeft, 0);
    }

    private void updateRecruitCapacity(Integer newCapacity) {
        if (newCapacity < this.recruitCount) {
            throw InvalidRecruitCapacityException.insufficientCapacity(this.recruitCount, newCapacity);
        }
        this.recruitCapacity = newCapacity;
    }

    private void validateUpdatePermission() {
        if (isTravelStarted()) {
            throw new TravelAlreadyStartedException();
        }
        if (isTravelEnded()) {
            throw new TravelAlreadyStartedException();
        }
    }

    private boolean isRecruitmentClosed() {
        return LocalDate.now().isAfter(recruitDeadline);
    }

    private boolean isTravelStarted() {
        return LocalDate.now().isAfter(startDate);
    }

    private boolean isTravelEnded() {
        return LocalDate.now().isAfter(endDate);
    }
    
    public void incrementRecruitCount() {
        if (this.recruitCount >= this.recruitCapacity) {
            throw new RecruitCountExceedCapacityException();
        }
        this.recruitCount++;
    }
    
    public void decrementRecruitCount() {
        if (this.recruitCount <= 0) {
            throw new RecruitCountBelowZeroException();
        }
        this.recruitCount--;
    }
}
