package com.dduru.gildongmu.post.dto;

import com.dduru.gildongmu.auth.enums.AgeRange;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PostCreateRequest {
    @NotNull(message = "여행지 ID는 필수입니다")
    private Long destinationId;

    @NotBlank(message = "제목은 필수입니다")
    @Size(min = 5, max = 100, message = "제목은 5자 이상 100자 이하여야 합니다")
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    @Size(min = 10, max = 5000, message = "내용은 10자 이상 5000자 이하여야 합니다")
    private String content;

    @NotNull(message = "여행 시작일은 필수입니다")
    @Future(message = "여행 시작일은 미래여야 합니다")
    private LocalDate startDate;

    @NotNull(message = "여행 종료일은 필수입니다")
    @Future(message = "여행 종료일은 미래여야 합니다")
    private LocalDate endDate;

    @NotNull(message = "모집 인원은 필수입니다")
    @Min(value = 1, message = "모집 인원은 최소 1명 이상이어야 합니다")
    @Max(value = 10, message = "모집 인원은 최대 10명까지 가능합니다")
    private Integer recruitCapacity;

    @NotNull(message = "모집 마감일은 필수입니다")
    @Future(message = "모집 마감일은 미래여야 합니다")
    private LocalDate recruitDeadline;

    private String preferredGender;

    private String preferredAgeMin;

    private String preferredAgeMax;

    @Min(value = 0, message = "예산은 0원 이상이어야 합니다")
    private Integer budgetMin;

    @Min(value = 0, message = "예산은 0원 이상이어야 합니다")
    private Integer budgetMax;

    @Size(max = 5, message = "사진은 최대 5장까지 업로드 가능합니다")
    private List<String> photoUrls;

    @Size(max = 10, message = "태그는 최대 10개까지 가능합니다")
    private List<String> tags;

    @AssertTrue(message = "여행 종료일은 시작일 이후여야 합니다")
    private boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return endDate.isAfter(startDate) || endDate.isEqual(startDate);
    }

    @AssertTrue(message = "모집 마감일은 여행 시작일 이전이어야 합니다")
    private boolean isRecruitDeadlineBeforeStartDate() {
        if (recruitDeadline == null || startDate == null) {
            return true;
        }
        return recruitDeadline.isBefore(startDate);
    }

    @AssertTrue(message = "최대 예산은 최소 예산보다 커야 합니다")
    private boolean isBudgetValid() {
        if (budgetMin == null || budgetMax == null) {
            return true;
        }
        return budgetMax >= budgetMin;
    }

    @AssertTrue(message = "최대 연령은 최소 연령보다 크거나 같아야 합니다")
    private boolean isAgeRangeValid() {
        if (preferredAgeMin == null || preferredAgeMax == null) {
            return true;
        }
        try {
            AgeRange minAge = AgeRange.valueOf(preferredAgeMin);
            AgeRange maxAge = AgeRange.valueOf(preferredAgeMax);
            return minAge.ordinal() <= maxAge.ordinal();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
