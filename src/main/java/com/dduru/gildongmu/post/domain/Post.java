package com.dduru.gildongmu.post.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.exception.RecruitmentClosedException;
import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.post.domain.enums.PostStatus;
import com.dduru.gildongmu.post.exception.InvalidPostContentException;
import com.dduru.gildongmu.post.exception.InvalidPostDateException;
import com.dduru.gildongmu.post.exception.InvalidPostTitleException;
import com.dduru.gildongmu.post.exception.InvalidPreferredAgeException;
import com.dduru.gildongmu.post.exception.InvalidRecruitCapacityException;
import com.dduru.gildongmu.post.exception.RecruitDeadlinePassedException;
import com.dduru.gildongmu.post.exception.RecruitCountBelowZeroException;
import com.dduru.gildongmu.post.exception.RecruitCountExceedCapacityException;
import com.dduru.gildongmu.post.exception.TravelAlreadyEndedException;
import com.dduru.gildongmu.post.exception.TravelAlreadyStartedException;
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

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    private static final int TITLE_MIN = 5;
    private static final int TITLE_MAX = 40;
    private static final int CONTENT_MIN = 20;
    private static final int CONTENT_MAX = 1000;
    private static final int MIN_PREFERRED_AGE = 20;
    private static final int MAX_PREFERRED_AGE = 100;

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
    @ColumnDefault("1")
    private Integer recruitCount = 1;

    @Column(name = "recruit_deadline")
    private LocalDate recruitDeadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_gender", nullable = false)
    private Gender preferredGender;

    @Column(name = "is_age_any", nullable = false)
    @ColumnDefault("false")
    private boolean isAgeAny;

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_age")
    private Integer maxAge;

    @Column(name = "photo_url", columnDefinition = "TEXT")
    private String photoUrl;

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
    @Column(name = "companion_type")
    private CompanionType companionType;

    @Builder(access = AccessLevel.PRIVATE)
    private Post(User user, Destination destination, String title, String content,
                LocalDate startDate, LocalDate endDate, Integer recruitCapacity,
                LocalDate recruitDeadline, Gender preferredGender,
                boolean isAgeAny, Integer minAge, Integer maxAge,
                String photoUrl, String tags, CompanionType companionType) {
        this.user = user;
        this.destination = destination;
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.recruitCapacity = recruitCapacity;
        this.recruitCount = 1;
        this.recruitDeadline = recruitDeadline;
        this.preferredGender = preferredGender;
        this.isAgeAny = isAgeAny;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.photoUrl = photoUrl;
        this.tags = tags;
        this.viewCount = 0;
        this.likeCount = 0;
        this.isDeleted = false;
        this.companionType = companionType;
    }

    public static Post createPost(User user, Destination destination, String title, String content,
                                  LocalDate startDate, LocalDate endDate, Integer recruitCapacity,
                                  LocalDate recruitDeadline, Gender preferredGender,
                                  boolean isAgeAny, Integer minAge, Integer maxAge,
                                  String photoUrl, String tags, CompanionType companionType) {
        validateDateRange(startDate, endDate);

        return Post.builder()
                .user(user)
                .destination(destination)
                .title(requireValidTitle(title))
                .content(requireValidContent(content))
                .startDate(startDate)
                .endDate(endDate)
                .recruitCapacity(recruitCapacity)
                .recruitDeadline(recruitDeadline)
                .preferredGender(preferredGender)
                .isAgeAny(isAgeAny)
                .minAge(minAge)
                .maxAge(maxAge)
                .photoUrl(photoUrl)
                .tags(tags)
                .companionType(companionType)
                .build();
    }

    public void updatePost(Destination destination, String title, String content,
                           LocalDate startDate, LocalDate endDate, Integer recruitCapacity,
                           LocalDate recruitDeadline, Gender preferredGender,
                           boolean isAgeAny, Integer minAge, Integer maxAge,
                           String photoUrl, String tags, CompanionType companionType,
                           LocalDate today) {
        validateUpdatable(today);
        applyBasicChanges(destination, title, content, startDate, endDate, recruitDeadline,
                preferredGender, tags, companionType);
        this.isAgeAny = isAgeAny;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.photoUrl = photoUrl;
        applyRecruitCapacity(recruitCapacity);
    }

    public void softDelete(Long userId) {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = userId;
    }

    public void changeStatus(PostStatus newStatus) {
        this.status = newStatus;
    }

    public void approveParticipation(Participation participation) {
        participation.approve();
        incrementRecruitCount();
    }

    public void decrementRecruitCountIfApproved(Participation participation) {
        if (!participation.isApproved()) {
            return;
        }

        decrementRecruitCount();
    }

    public void increaseLikes() {
        this.likeCount++;
    }

    public void decreaseLikes() {
        this.likeCount--;
    }

    public boolean isFull() {
        return this.recruitCount >= this.recruitCapacity;
    }

    public boolean isClosed() {
        return this.status == PostStatus.CLOSED;
    }

    public void validateIsOpen() {
        if (isFull()) {
            throw RecruitmentClosedException.isFulled();
        }
        if (isClosed()) {
            throw RecruitmentClosedException.isClosed();
        }
    }

    public int getDaysUntilRecruitDeadline(LocalDate today) {
        if (recruitDeadline == null) {
            return Integer.MAX_VALUE;
        }
        return Math.max(daysBetween(today, recruitDeadline), 0);
    }

    public int getDaysUntilTravelStart(LocalDate today) {
        return daysBetween(today, startDate);
    }

    public static void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new InvalidPostDateException();
        }
    }

    public static void validatePreferredAge(boolean isAgeAny, Integer minAge, Integer maxAge) {
        if (isAgeAny) {
            if (minAge != null || maxAge != null) {
                throw InvalidPreferredAgeException.conflictWithAgeAny();
            }
            return;
        }
        if (minAge == null || maxAge == null) {
            throw InvalidPreferredAgeException.incompleteRange();
        }
        if (minAge < MIN_PREFERRED_AGE || maxAge > MAX_PREFERRED_AGE || minAge > maxAge) {
            throw InvalidPreferredAgeException.outOfBounds(MIN_PREFERRED_AGE, MAX_PREFERRED_AGE);
        }
    }

    private void applyBasicChanges(Destination destination, String title, String content,
                                   LocalDate startDate, LocalDate endDate, LocalDate recruitDeadline,
                                   Gender preferredGender, String tags, CompanionType companionType) {
        if (destination != null) {
            this.destination = destination;
        }
        if (title != null) {
            this.title = requireValidTitle(title);
        }
        if (content != null) {
            this.content = requireValidContent(content);
        }
        if (startDate != null) {
            this.startDate = startDate;
        }
        if (endDate != null) {
            this.endDate = endDate;
        }
        if (recruitDeadline != null) {
            this.recruitDeadline = recruitDeadline;
        }
        if (preferredGender != null) {
            this.preferredGender = preferredGender;
        }
        if (tags != null) {
            this.tags = tags;
        }
        if (companionType != null) {
            this.companionType = companionType;
        }
    }

    private void applyRecruitCapacity(Integer recruitCapacity) {
        if (recruitCapacity != null) {
            updateRecruitCapacity(recruitCapacity);
        }
    }

    private static int daysBetween(LocalDate from, LocalDate target) {
        return (int) ChronoUnit.DAYS.between(from, target);
    }

    public void validateUpdatable(LocalDate today) {
        if (hasRecruitDeadlinePassed(today)) {
            throw new RecruitDeadlinePassedException();
        }
        if (hasTravelEnded(today)) {
            throw new TravelAlreadyEndedException();
        }
        if (hasTravelStarted(today)) {
            throw new TravelAlreadyStartedException();
        }
    }

    public boolean hasRecruitDeadlinePassed(LocalDate today) {
        return recruitDeadline != null && today.isAfter(recruitDeadline);
    }

    public boolean hasTravelStarted(LocalDate today) {
        return today.isAfter(startDate);
    }

    public boolean hasTravelEnded(LocalDate today) {
        return today.isAfter(endDate);
    }

    private void updateRecruitCapacity(Integer newCapacity) {
        if (newCapacity < this.recruitCount) {
            throw new InvalidRecruitCapacityException();
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
        if (this.recruitCount <= 1) {
            throw new RecruitCountBelowZeroException();
        }
        this.recruitCount--;
    }

    private static String requireValidTitle(String title) {
        if (title.length() < TITLE_MIN || title.length() > TITLE_MAX) {
            throw new InvalidPostTitleException();
        }
        return title;
    }

    private static String requireValidContent(String content) {
        if (content.length() < CONTENT_MIN || content.length() > CONTENT_MAX) {
            throw new InvalidPostContentException();
        }
        return content;
    }
}
