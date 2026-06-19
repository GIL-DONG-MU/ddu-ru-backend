package com.dduru.gildongmu.journey.dto.request;

import com.dduru.gildongmu.journey.domain.enums.JourneyRoleType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record JourneyMemberRoleUpdateRequest(
        @NotNull(message = "roles는 필수입니다.")
        @Valid List<RoleLabelRequest> roles
) {
    public record RoleLabelRequest(
            @NotNull(message = "역할 타입은 필수입니다.")
            JourneyRoleType roleType,

            String customRoleLabel
    ) {
        public RoleLabelRequest {
            if (customRoleLabel != null) customRoleLabel = customRoleLabel.strip();
        }
    }
}
