package com.dduru.gildongmu.post.dto.request;

import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.post.domain.enums.RecruitMethod;
import com.dduru.gildongmu.post.domain.enums.RecruitType;
import com.dduru.gildongmu.profile.domain.enums.AgeRange;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record PostCreateRequest(
        @NotNull(message = "여행지 ID는 필수입니다")
        Long destinationId,

        @NotBlank(message = "제목은 필수입니다")
        @Size(min = 5, max = 40, message = "제목은 5자 이상 40자 이하여야 합니다")
        String title,

        @NotBlank(message = "내용은 필수입니다")
        @Size(min = 20, max = 1000, message = "내용은 20자 이상 1000자 이하여야 합니다")
        String content,

        @NotNull(message = "여행 시작일은 필수입니다")
        @FutureOrPresent(message = "여행 시작일은 오늘 이후여야 합니다")
        LocalDate startDate,

        @NotNull(message = "여행 종료일은 필수입니다")
        @FutureOrPresent(message = "여행 종료일은 오늘 이후여야 합니다")
        LocalDate endDate,

        @NotNull(message = "모집 인원은 필수입니다")
        @Min(value = 2, message = "모집 인원은 최소 2명 이상이어야 합니다")
        @Max(value = 10, message = "모집 인원은 최대 10명까지 가능합니다")
        Integer recruitCapacity,

        @FutureOrPresent(message = "모집 마감일은 오늘 이후여야 합니다")
        LocalDate recruitDeadline,

        @NotNull(message = "선호하는 성별을 골라주세요")
        Gender preferredGender,

        @Size(max = 3, message = "선호 연령대는 최대 3개까지 선택할 수 있습니다")
        List<AgeRange> preferredAges,

        @Min(value = 0, message = "예산은 0원 이상이어야 합니다")
        Integer budgetMin,

        @Min(value = 0, message = "예산은 0원 이상이어야 합니다")
        Integer budgetMax,

        @Size(max = 5, message = "사진은 최대 5장까지 업로드 가능합니다")
        List<String> photoUrls,

        @Size(max = 10, message = "태그는 최대 10개까지 가능합니다")
        List<String> tags,

        @NotNull(message = "모집 공개 방식은 필수입니다")
        RecruitType recruitType,

        @NotNull(message = "모집 기간 방식은 필수입니다")
        RecruitMethod recruitMethod,

        CompanionType companionType
) {
}
