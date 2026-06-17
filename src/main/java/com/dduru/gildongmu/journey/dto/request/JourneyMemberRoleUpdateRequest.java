package com.dduru.gildongmu.journey.dto.request;

import com.dduru.gildongmu.journey.domain.enums.JourneyRoleType;
import jakarta.validation.constraints.NotNull;

public record JourneyMemberRoleUpdateRequest(
        @NotNull(message = "역할 타입은 필수입니다.")
        JourneyRoleType roleType,

        String customRoleLabel
) {
    public JourneyMemberRoleUpdateRequest {
        if (customRoleLabel != null) customRoleLabel = customRoleLabel.strip();
    }
}
