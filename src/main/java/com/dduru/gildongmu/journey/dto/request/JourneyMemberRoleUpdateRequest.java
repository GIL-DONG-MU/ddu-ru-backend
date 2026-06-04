package com.dduru.gildongmu.journey.dto.request;

import com.dduru.gildongmu.journey.domain.enums.JourneyRoleType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record JourneyMemberRoleUpdateRequest(
        @NotNull(message = "역할 타입은 필수입니다.")
        JourneyRoleType roleType,

        @Size(max = 20, message = "직접 입력 역할명은 20자 이하여야 합니다.")
        String customRoleLabel
) {
}
