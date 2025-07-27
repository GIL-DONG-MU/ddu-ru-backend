package com.dduru.gildongmu.post.dto;

import com.dduru.gildongmu.auth.enums.AgeRange;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record PostCreateRequest(
        @NotNull(message = "여행지 ID는 필수입니다")
        Long destinationId,

        @NotBlank(message = "제목은 필수입니다")
        @Size(min = 5, max = 100, message = "제목은 5자 이상 100자 이하여야 합니다")
        String title,

        @NotBlank(message = "내용은 필수입니다")
        @Size(min = 10, max = 5000, message = "내용은 10자 이상 5000자 이하여야 합니다")
        String content,

        @NotNull(message = "여행 시작일은 필수입니다")
        @Future(message = "여행 시작일은 미래여야 합니다")
        LocalDate startDate,

        @NotNull(message = "여행 종료일은 필수입니다")
        @Future(message = "여행 종료일은 미래여야 합니다")
        LocalDate endDate,

        @NotNull(message = "모집 인원은 필수입니다")
        @Min(value = 1, message = "모집 인원은 최소 1명 이상이어야 합니다")
        @Max(value = 10, message = "모집 인원은 최대 10명까지 가능합니다")
        Integer recruitCapacity,

        @NotNull(message = "모집 마감일은 필수입니다")
        @Future(message = "모집 마감일은 미래여야 합니다")
        LocalDate recruitDeadline,

        String preferredGender,
        String preferredAgeMin,
        String preferredAgeMax,

        @Min(value = 0, message = "예산은 0원 이상이어야 합니다")
        Integer budgetMin,

        @Min(value = 0, message = "예산은 0원 이상이어야 합니다")
        Integer budgetMax,

        @Size(max = 5, message = "사진은 최대 5장까지 업로드 가능합니다")
        List<String> photoUrls,

        @Size(max = 10, message = "태그는 최대 10개까지 가능합니다")
        List<String> tags
) {
    public PostCreateRequest {
        // 여행 종료일이 시작일 이전이면 예외
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("여행 종료일은 시작일 이후여야 합니다");
        }
        // 모집 마감일이 시작일 이후면 예외
        if (recruitDeadline != null && startDate != null && !recruitDeadline.isBefore(startDate)) {
            throw new IllegalArgumentException("모집 마감일은 여행 시작일 이전이어야 합니다");
        }
        // 최대 예산 < 최소 예산이면 예외
        if (budgetMin != null && budgetMax != null && budgetMax < budgetMin) {
            throw new IllegalArgumentException("최대 예산은 최소 예산보다 커야 합니다");
        }
        // 연령대 유효성
        if (preferredAgeMin != null && preferredAgeMax != null) {
            try {
                AgeRange minAge = AgeRange.valueOf(preferredAgeMin);
                AgeRange maxAge = AgeRange.valueOf(preferredAgeMax);
                if (minAge.ordinal() > maxAge.ordinal()) {
                    throw new IllegalArgumentException("최대 연령은 최소 연령보다 크거나 같아야 합니다");
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("연령대 값이 올바르지 않습니다", e);
            }
        }
    }
}
