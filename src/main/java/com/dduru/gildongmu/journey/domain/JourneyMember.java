package com.dduru.gildongmu.journey.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberRole;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import com.dduru.gildongmu.journey.exception.InvalidJourneyMemberRoleException;
import com.dduru.gildongmu.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private static final int MAX_ROLE_COUNT = 5;

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

    @OneToMany(mappedBy = "journeyMember", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private List<JourneyMemberRoleLabel> roleLabels = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private JourneyMember(Journey journey, User user, JourneyMemberRole role, LocalDateTime joinedAt) {
        this.journey = journey;
        this.user = user;
        this.role = role;
        this.status = JourneyMemberStatus.ACTIVE;
        this.joinedAt = joinedAt;
    }

    public static JourneyMember createHost(Journey journey, User user, LocalDateTime joinedAt) {
        return JourneyMember.builder()
                .journey(journey)
                .user(user)
                .role(JourneyMemberRole.HOST)
                .joinedAt(joinedAt)
                .build();
    }

    public static JourneyMember createMember(Journey journey, User user, LocalDateTime joinedAt) {
        return JourneyMember.builder()
                .journey(journey)
                .user(user)
                .role(JourneyMemberRole.MEMBER)
                .joinedAt(joinedAt)
                .build();
    }

    public void remove(LocalDateTime removedAt) {
        this.status = JourneyMemberStatus.REMOVED;
        this.removedAt = removedAt;
    }

    public boolean isHost() {
        return role == JourneyMemberRole.HOST;
    }

    public void replaceRoleLabels(List<JourneyMemberRoleLabel> newLabels) {
        if (newLabels.size() > MAX_ROLE_COUNT) {
            throw InvalidJourneyMemberRoleException.roleLimitExceeded();
        }
        validateNoDuplicateCustomLabels(newLabels);
        this.roleLabels.clear();
        this.roleLabels.addAll(newLabels);
    }

    private static void validateNoDuplicateCustomLabels(List<JourneyMemberRoleLabel> labels) {
        List<String> customLabels = labels.stream()
                .filter(JourneyMemberRoleLabel::isCustom)
                .map(JourneyMemberRoleLabel::getCustomRoleLabel)
                .toList();
        Set<String> seen = new HashSet<>();
        for (String label : customLabels) {
            if (!seen.add(label)) {
                throw InvalidJourneyMemberRoleException.duplicateCustomRoleLabel();
            }
        }
    }
}
