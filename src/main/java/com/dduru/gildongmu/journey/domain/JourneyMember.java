package com.dduru.gildongmu.journey.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberRole;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import com.dduru.gildongmu.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "journey_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"journey_id", "user_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JourneyMember extends BaseTimeEntity {
    /**
     * 실제 여행멤버들을 나타낸다.
     * 참여 신청 이력(participations)과 분리해 host/member를 동일한 축에서 조회하기 위해 사용한다.
     */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_id", nullable = false)
    private Journey journey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private JourneyMemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JourneyMemberStatus status;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "removed_at")
    private LocalDateTime removedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private JourneyMember(Journey journey, User user, JourneyMemberRole role) {
        this.journey = journey;
        this.user = user;
        this.role = role;
        this.status = JourneyMemberStatus.ACTIVE;
        this.joinedAt = LocalDateTime.now();
    }

    public static JourneyMember createHost(Journey journey, User user) {
        return JourneyMember.builder()
                .journey(journey)
                .user(user)
                .role(JourneyMemberRole.HOST)
                .build();
    }

    public static JourneyMember createMember(Journey journey, User user) {
        return JourneyMember.builder()
                .journey(journey)
                .user(user)
                .role(JourneyMemberRole.MEMBER)
                .build();
    }

    public void activate() {
        this.status = JourneyMemberStatus.ACTIVE;
        this.removedAt = null;
    }

    public void remove(LocalDateTime removedAt) {
        this.status = JourneyMemberStatus.REMOVED;
        this.removedAt = removedAt;
    }

    public boolean isHost() {
        return role == JourneyMemberRole.HOST;
    }
}
