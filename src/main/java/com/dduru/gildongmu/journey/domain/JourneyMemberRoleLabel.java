package com.dduru.gildongmu.journey.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.journey.domain.enums.JourneyRoleType;
import com.dduru.gildongmu.journey.exception.InvalidJourneyMemberRoleException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Entity
@Table(
        name = "journey_member_role_labels",
        indexes = @Index(name = "idx_role_labels_member", columnList = "journey_member_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JourneyMemberRoleLabel extends BaseTimeEntity {

    private static final int CUSTOM_ROLE_LABEL_MAX_LENGTH = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_member_id", nullable = false)
    private JourneyMember journeyMember;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", length = 20, nullable = false)
    private JourneyRoleType roleType;

    @Column(name = "custom_role_label", length = 10)
    private String customRoleLabel;

    private JourneyMemberRoleLabel(JourneyMember journeyMember, JourneyRoleType roleType, String customRoleLabel) {
        this.journeyMember = journeyMember;
        this.roleType = roleType;
        this.customRoleLabel = customRoleLabel;
    }

    public boolean isCustom() {
        return roleType == JourneyRoleType.CUSTOM;
    }

    public static JourneyMemberRoleLabel of(JourneyMember member, JourneyRoleType roleType, String customRoleLabel) {
        if (roleType == JourneyRoleType.CUSTOM) {
            validateCustomRoleLabel(customRoleLabel);
            return new JourneyMemberRoleLabel(member, roleType, customRoleLabel);
        }
        return new JourneyMemberRoleLabel(member, roleType, null);
    }

    private static void validateCustomRoleLabel(String label) {
        if (!StringUtils.hasText(label)) {
            throw InvalidJourneyMemberRoleException.invalidCustomRoleLabel();
        }
        if (label.codePointCount(0, label.length()) > CUSTOM_ROLE_LABEL_MAX_LENGTH) {
            throw InvalidJourneyMemberRoleException.invalidCustomRoleLabel();
        }
    }
}
