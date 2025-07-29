package com.dduru.gildongmu.post.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record PostUpdateRequest(
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
}
