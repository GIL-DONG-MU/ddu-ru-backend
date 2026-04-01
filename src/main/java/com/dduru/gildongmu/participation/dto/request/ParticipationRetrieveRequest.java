package com.dduru.gildongmu.participation.dto.request;

import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "나에게 온 동행 신청 목록 조회 조건")
public record ParticipationRetrieveRequest(
        @Schema(
                description = "조회할 신청 상태. 미지정 시 전체 조회",
                example = "PENDING",
                allowableValues = {"PENDING", "CONTACTING", "APPROVED", "REJECTED"}
        )
        ParticipationStatus status
) {
}
