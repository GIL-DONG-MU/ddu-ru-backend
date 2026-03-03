package com.dduru.gildongmu.post.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.post.domain.enums.PostStatus;
import com.dduru.gildongmu.post.domain.enums.RecruitMethod;
import com.dduru.gildongmu.post.domain.enums.RecruitType;
import com.dduru.gildongmu.post.exception.InvalidPostStatusException;
import com.dduru.gildongmu.post.exception.InvalidRecruitCapacityException;
import com.dduru.gildongmu.post.exception.RecruitDeadlinePassedException;
import com.dduru.gildongmu.post.exception.RecruitCountBelowZeroException;
import com.dduru.gildongmu.post.exception.RecruitCountExceedCapacityException;
import com.dduru.gildongmu.post.exception.TravelAlreadyEndedException;
import com.dduru.gildongmu.post.exception.TravelAlreadyStartedException;
import com.dduru.gildongmu.profile.domain.enums.AgeRange;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

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
    private Integer recruitCapacity;

    @Column(name = "recruit_count", nullable = false)
    @ColumnDefault("0")
    private Integer recruitCount = 0;

    @Column(name = "recruit_deadline")
    private LocalDate recruitDeadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_gender", nullable = false)
    private Gender preferredGender;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "post_preferred_ages", joinColumns = @JoinColumn(name = "post_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "age_range", nullable = false)
    private List<AgeRange> preferredAges = new ArrayList<>();

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
    private int viewCount = 0;

    @Column(name = "like_count", nullable = false)
    @ColumnDefault("0")
    private int likeCount = 0;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "recruit_type", nullable = false)
    private RecruitType recruitType;

    @Enumerated(EnumType.STRING)
    @Column(name = "recruit_method", nullable = false)
    private RecruitMethod recruitMethod;

    @Builder
    public Post(User user, Destination destination, String title, String content,
                LocalDate startDate, LocalDate endDate, Integer recruitCapacity,
                LocalDate recruitDeadline, Gender preferredGender, List<AgeRange> preferredAges,
                Integer budgetMin, Integer budgetMax, String photoUrls, String tags, RecruitType recruitType, RecruitMethod recruitMethod) {
        this.user = user;
        this.destination = destination;
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.recruitCapacity = recruitCapacity;
        this.recruitCount = 0;
        this.recruitDeadline = recruitDeadline;
        this.preferredGender = preferredGender;
        this.preferredAges = preferredAges;
        this.budgetMin = budgetMin;
        this.budgetMax = budgetMax;
        this.photoUrls = photoUrls;
        this.tags = tags;
        this.viewCount = 0;
        this.recruitType = recruitType;
        this.recruitMethod = recruitMethod;
    }

    public static Post createPost(User user, Destination destination, String title, String content,
                                  LocalDate startDate, LocalDate endDate, Integer recruitCapacity,
                                  LocalDate recruitDeadline, Gender preferredGender, List<AgeRange> preferredAges,
                                  Integer budgetMin, Integer budgetMax, String photoUrls, String tags, RecruitType recruitType, RecruitMethod recruitMethod) {

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
                .preferredAges(preferredAges)
                .budgetMin(budgetMin)
                .budgetMax(budgetMax)
                .photoUrls(photoUrls)
                .tags(tags)
                .recruitType(recruitType)
                .recruitMethod(recruitMethod)
                .build();
    }

    public void updatePost(Destination destination, String title, String content,
                           LocalDate startDate, LocalDate endDate, Integer recruitCapacity,
                           LocalDate recruitDeadline, Gender preferredGender, List<AgeRange> preferredAges,
                           Integer budgetMin, Integer budgetMax, String photoUrls, String tags, RecruitType recruitType, RecruitMethod recruitMethod) {
        validateUpdatePermission();

        if (destination != null) this.destination = destination;
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        if (startDate != null) this.startDate = startDate;
        if (endDate != null) this.endDate = endDate;
        if (recruitDeadline != null) this.recruitDeadline = recruitDeadline;
        if (preferredGender != null) this.preferredGender = preferredGender;
        if (preferredAges != null) this.preferredAges = preferredAges;
        if (budgetMin != null) this.budgetMin = budgetMin;
        if (budgetMax != null) this.budgetMax = budgetMax;
        if (photoUrls != null) this.photoUrls = photoUrls;
        if (tags != null) this.tags = tags;
        if (recruitCapacity != null) updateRecruitCapacity(recruitCapacity);
        if (recruitType != null) this.recruitType = recruitType;
        if (recruitMethod != null) this.recruitMethod = recruitMethod;
    }

    public void softDelete(Long userId) {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = userId;
    }

    public void updateStatus(PostStatus newStatus) {
        if (this.status == PostStatus.FULL && newStatus == PostStatus.OPEN) {
            throw InvalidPostStatusException.cannotTransition(this.status, newStatus);
        }
        this.status = newStatus;
    }

    public void approveParticipation(Participation participation) {
        participation.approve();
        this.incrementRecruitCount();
        if (this.recruitCount >= this.recruitCapacity) {
            this.updateStatus(PostStatus.FULL);
        }
    }

    public void removeApprovedParticipation(Participation participation) {
        if (participation.isApproved()) {
            this.decrementRecruitCount();
            reopenIfParticipantRemoved();
        }
    }

    private void reopenIfParticipantRemoved() {
        if (this.status == PostStatus.FULL && this.recruitCount < this.recruitCapacity) {
            this.status = PostStatus.OPEN;
        }
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        this.likeCount--;
    }

    public boolean isRecruitOpen() {
        return status == PostStatus.OPEN;
    }

    public int getDaysUntilRecruitDeadline() {
        int daysLeft = (int) ChronoUnit.DAYS.between(LocalDate.now(), recruitDeadline) + 1;
        return Math.max(daysLeft, 0);
    }

    public int getDaysUntilTravelStart() {
        return (int) ChronoUnit.DAYS.between(LocalDate.now(), startDate);
    }

    private void validateUpdatePermission() {
        if (isRecruitDeadlinePassed()) {
            throw new RecruitDeadlinePassedException();
        }
        if (isTravelEnded()) {
            throw new TravelAlreadyEndedException();
        }
        if (isTravelStarted()) {
            throw new TravelAlreadyStartedException();
        }
    }

    private boolean isRecruitDeadlinePassed() {
        return LocalDate.now().isAfter(recruitDeadline);
    }

    private boolean isTravelStarted() {
        return LocalDate.now().isAfter(startDate);
    }

    private boolean isTravelEnded() {
        return LocalDate.now().isAfter(endDate);
    }

    private void updateRecruitCapacity(Integer newCapacity) {
        if (newCapacity < this.recruitCount) {
            throw InvalidRecruitCapacityException.insufficientCapacity(this.recruitCount, newCapacity);
        }
        this.recruitCapacity = newCapacity;
    }

    private void incrementRecruitCount() {
        if (this.recruitCount >= this.recruitCapacity) {
            throw new RecruitCountExceedCapacityException();
        }
        this.recruitCount++;
    }

    private void decrementRecruitCount() {
        if (this.recruitCount <= 0) {
            throw new RecruitCountBelowZeroException();
        }
        this.recruitCount--;
    }
}
