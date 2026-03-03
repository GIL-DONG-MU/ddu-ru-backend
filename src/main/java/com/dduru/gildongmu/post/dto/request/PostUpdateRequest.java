package com.dduru.gildongmu.post.dto.request;

import com.dduru.gildongmu.post.domain.enums.RecruitMethod;
import com.dduru.gildongmu.post.domain.enums.RecruitType;
import com.dduru.gildongmu.profile.domain.enums.AgeRange;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record PostUpdateRequest(
        Long destinationId,

        @Size(min = 5, max = 100, message = "제목은 5자 이상 100자 이하여야 합니다")
        String title,

        @Size(min = 10, max = 5000, message = "내용은 10자 이상 5000자 이하여야 합니다")
        String content,

        @FutureOrPresent(message = "여행 시작일은 오늘 이후여야 합니다")
        LocalDate startDate,

        @FutureOrPresent(message = "여행 종료일은 오늘 이후여야 합니다")
        LocalDate endDate,

        @Min(value = 2, message = "모집 인원은 최소 2명 이상이어야 합니다")
        @Max(value = 10, message = "모집 인원은 최대 10명까지 가능합니다")
        Integer recruitCapacity,

        @FutureOrPresent(message = "모집 마감일은 오늘 이후여야 합니다")
        LocalDate recruitDeadline,

        Gender preferredGender,
        AgeRange preferredAgeMin,
        AgeRange preferredAgeMax,

        @Min(value = 0, message = "예산은 0원 이상이어야 합니다")
        Integer budgetMin,

        @Min(value = 0, message = "예산은 0원 이상이어야 합니다")
        Integer budgetMax,

        @Size(max = 5, message = "사진은 최대 5장까지 업로드 가능합니다")
        List<String> photoUrls,

        @Size(max = 10, message = "태그는 최대 10개까지 가능합니다")
        List<String> tags,

        RecruitType recruitType,

        RecruitMethod recruitMethod
) {
}
