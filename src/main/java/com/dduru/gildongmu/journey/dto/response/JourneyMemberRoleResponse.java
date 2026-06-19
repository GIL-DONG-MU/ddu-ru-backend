package com.dduru.gildongmu.journey.dto.response;

import com.dduru.gildongmu.journey.domain.JourneyMember;

import java.util.List;

public record JourneyMemberRoleResponse(
        Long memberUserId,
        List<RoleLabelInfo> roles
) {
    public static JourneyMemberRoleResponse from(JourneyMember member) {
        return new JourneyMemberRoleResponse(
                member.getUser().getId(),
                member.getRoleLabels().stream()
                        .map(RoleLabelInfo::from)
                        .toList()
        );
    }
}
