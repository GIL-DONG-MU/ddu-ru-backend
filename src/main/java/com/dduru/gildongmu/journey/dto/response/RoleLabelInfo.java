package com.dduru.gildongmu.journey.dto.response;

import com.dduru.gildongmu.journey.domain.JourneyMemberRoleLabel;
import com.dduru.gildongmu.journey.domain.enums.JourneyRoleType;

public record RoleLabelInfo(
        JourneyRoleType roleType,
        String customRoleLabel
) {
    public static RoleLabelInfo from(JourneyMemberRoleLabel label) {
        return new RoleLabelInfo(label.getRoleType(), label.getCustomRoleLabel());
    }
}
