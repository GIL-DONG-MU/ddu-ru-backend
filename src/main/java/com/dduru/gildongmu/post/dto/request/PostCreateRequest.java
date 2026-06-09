package com.dduru.gildongmu.post.dto.request;

import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record PostCreateRequest(
        @NotNull(message = "여행지 ID는 필수입니다")
        Long destinationId,

        @NotBlank(message = "제목은 필수입니다")
        String title,

        @NotBlank(message = "내용은 필수입니다")
        String content,

        @NotNull(message = "여행 시작일은 필수입니다")
        @FutureOrPresent(message = "여행 시작일은 오늘 이후여야 합니다")
        LocalDate startDate,

        @NotNull(message = "여행 종료일은 필수입니다")
        @FutureOrPresent(message = "여행 종료일은 오늘 이후여야 합니다")
        LocalDate endDate,

        @NotNull(message = "모집 인원은 필수입니다")
        @Min(value = 2, message = "모집 인원은 최소 2명 이상이어야 합니다 (호스트 포함)")
        @Max(value = 10, message = "모집 인원은 최대 10명까지 가능합니다 (호스트 포함)")
        Integer recruitCapacity,

        @NotNull(message = "선호하는 성별을 골라주세요")
        Gender preferredGender,

        @NotNull(message = "선호 연령 설정 여부는 필수입니다")
        Boolean isAgeAny,

        Integer minAge,
        Integer maxAge,

        String photoUrl,

        List<String> tags,

        @NotNull(message = "동행 방식은 필수입니다")
        CompanionType companionType
) {
    public PostCreateRequest {
        if (title != null) title = title.trim();
        if (content != null) content = content.trim();
        if (photoUrl != null) photoUrl = photoUrl.trim();
        if (tags != null) {
            tags = tags.stream()
                    .filter(t -> t != null && !t.strip().isEmpty())
                    .map(String::strip)
                    .toList();
        }
    }
}
