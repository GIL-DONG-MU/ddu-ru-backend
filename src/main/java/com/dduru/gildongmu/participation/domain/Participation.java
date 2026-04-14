package com.dduru.gildongmu.participation.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.participation.exception.InvalidParticipationStatusException;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "participations", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ParticipationStatus status = ParticipationStatus.PENDING;

    @Column(name = "message", length = 500)
    private String message;

    @Column(name = "contacted_at")
    private LocalDateTime contactedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Builder
    public Participation(Post post, User user, String message) {
        this.post = post;
        this.user = user;
        this.message = message;
        this.status = ParticipationStatus.PENDING;
    }

    public static Participation createParticipation(Post post, User user, String message) {
        return Participation.builder()
                .post(post)
                .user(user)
                .message(message)
                .build();
    }

    public void approve() {
        validateApprovalAllowed();
        this.status = ParticipationStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    public void contact() {
        validateContactAllowed();
        this.status = ParticipationStatus.CONTACTING;
        this.contactedAt = LocalDateTime.now();
    }

    public void reject() {
        validateRejectionAllowed();
        this.status = ParticipationStatus.REJECTED;
        this.rejectedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return status == ParticipationStatus.PENDING;
    }

    public boolean isContacting() {
        return status == ParticipationStatus.CONTACTING;
    }

    public boolean isRejected() {
        return status == ParticipationStatus.REJECTED;
    }

    public boolean isApproved() {
        return status == ParticipationStatus.APPROVED;
    }

    private void validateContactAllowed() {
        if (!isPending()) {
            throw InvalidParticipationStatusException.contactNotAllowed();
        }
    }

    private void validateApprovalAllowed() {
        if (!isPending() && !isContacting()) {
            throw InvalidParticipationStatusException.approvalNotAllowed();
        }
    }

    private void validateRejectionAllowed() {
        if (!isPending() && !isContacting()) {
            throw InvalidParticipationStatusException.rejectionNotAllowed();
        }
    }

    public void validateCancellableByApplicant() {
        if (!isPending()) {
            throw InvalidParticipationStatusException.cancelNotAllowed();
        }
    }
}
