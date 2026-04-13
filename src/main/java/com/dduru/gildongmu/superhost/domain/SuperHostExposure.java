package com.dduru.gildongmu.superhost.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.superhost.domain.enums.SuperHostExposureStatus;
import com.dduru.gildongmu.superhost.exception.SuperHostExposureInvalidPeriodException;
import com.dduru.gildongmu.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "super_host_exposures")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SuperHostExposure extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false, unique = true)
    private SuperHostTicket ticket;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at", nullable = false)
    private LocalDateTime endedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SuperHostExposureStatus status;

    @Builder
    public SuperHostExposure(SuperHostTicket ticket, User user, Post post, LocalDateTime startedAt, LocalDateTime endedAt) {
        validatePeriod(startedAt, endedAt);
        this.ticket = ticket;
        this.user = user;
        this.post = post;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.status = SuperHostExposureStatus.ACTIVE;
    }

    public static SuperHostExposure create(SuperHostTicket ticket, User user, Post post, LocalDateTime startedAt, LocalDateTime endedAt) {
        return SuperHostExposure.builder()
                .ticket(ticket)
                .user(user)
                .post(post)
                .startedAt(startedAt)
                .endedAt(endedAt)
                .build();
    }

    private void validatePeriod(LocalDateTime startedAt, LocalDateTime endedAt) {
        if (startedAt == null || endedAt == null || !endedAt.isAfter(startedAt)) {
            throw new SuperHostExposureInvalidPeriodException();
        }
    }
}
