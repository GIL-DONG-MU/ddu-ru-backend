package com.dduru.gildongmu.journey.dto.request;

import com.dduru.gildongmu.journey.domain.enums.JourneyRoleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record JourneyMemberRoleUpdateRequest(
        @Schema(description = "멤버에게 설정할 역할 목록. 기존 역할은 전체 교체되며 최대 5개까지 가능합니다.")
        @NotNull(message = "roles는 필수입니다.")
        @Valid List<RoleLabelRequest> roles
) {
    public record RoleLabelRequest(
            @Schema(description = "역할 타입. CUSTOM이면 customRoleLabel을 함께 전달합니다.", example = "CUSTOM")
            @NotNull(message = "역할 타입은 필수입니다.")
            JourneyRoleType roleType,

            @Schema(description = "직접 입력 역할명. roleType=CUSTOM일 때만 사용합니다.", example = "총무", nullable = true)
            String customRoleLabel
    ) {
        public RoleLabelRequest {
            if (customRoleLabel != null) customRoleLabel = customRoleLabel.strip();
        }
    }
}
