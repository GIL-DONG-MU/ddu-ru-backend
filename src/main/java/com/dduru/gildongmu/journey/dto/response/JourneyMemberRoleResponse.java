package com.dduru.gildongmu.journey.dto.response;

import com.dduru.gildongmu.journey.domain.JourneyMember;
import com.dduru.gildongmu.journey.domain.enums.JourneyRoleType;

public record JourneyMemberRoleResponse(
        Long memberUserId,
        JourneyRoleType roleType,
        String customRoleLabel
) {
    public static JourneyMemberRoleResponse from(JourneyMember member) {
        JourneyRoleType roleType = member.getRoleType();
        return new JourneyMemberRoleResponse(
                member.getUser().getId(),
                roleType,
                roleType == JourneyRoleType.CUSTOM ? member.getCustomRoleLabel() : null
        );
    }
}
