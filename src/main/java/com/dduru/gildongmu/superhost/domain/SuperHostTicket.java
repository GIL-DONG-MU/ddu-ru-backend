package com.dduru.gildongmu.superhost.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.superhost.domain.enums.SuperHostTicketSource;
import com.dduru.gildongmu.superhost.domain.enums.SuperHostTicketStatus;
import com.dduru.gildongmu.superhost.exception.SuperHostTicketAlreadyUsedException;
import com.dduru.gildongmu.superhost.exception.SuperHostTicketInvalidDurationException;
import com.dduru.gildongmu.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "super_host_tickets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SuperHostTicket extends BaseTimeEntity {
    private static final int MIN_DURATION_DAYS = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SuperHostTicketSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SuperHostTicketStatus status;

    @Column(name = "boost_duration_days", nullable = false)
    private int boostDurationDays;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Builder
    public SuperHostTicket(User user, SuperHostTicketSource source, int boostDurationDays) {
        validateBoostDurationDays(boostDurationDays);
        this.user = user;
        this.source = source;
        this.status = SuperHostTicketStatus.UNUSED;
        this.boostDurationDays = boostDurationDays;
    }

    public static SuperHostTicket create(User user, SuperHostTicketSource source, int boostDurationDays) {
        return SuperHostTicket.builder()
                .user(user)
                .source(source)
                .boostDurationDays(boostDurationDays)
                .build();
    }

    public void markUsed(LocalDateTime usedAt) {
        if (this.status != SuperHostTicketStatus.UNUSED) {
            throw new SuperHostTicketAlreadyUsedException();
        }
        this.status = SuperHostTicketStatus.USED;
        this.usedAt = usedAt;
    }

    private void validateBoostDurationDays(int boostDurationDays) {
        if (boostDurationDays < MIN_DURATION_DAYS) {
            throw new SuperHostTicketInvalidDurationException();
        }
    }
}
