package com.dduru.gildongmu.post.dto.request;

import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record PostUpdateRequest(
        Long destinationId,

        String title,

        String content,

        @FutureOrPresent(message = "여행 시작일은 오늘 이후여야 합니다")
        LocalDate startDate,

        @FutureOrPresent(message = "여행 종료일은 오늘 이후여야 합니다")
        LocalDate endDate,

        @Min(value = 2, message = "모집 인원은 최소 2명 이상이어야 합니다 (호스트 포함)")
        @Max(value = 10, message = "모집 인원은 최대 10명까지 가능합니다 (호스트 포함)")
        Integer recruitCapacity,

        Gender preferredGender,

        Boolean isAgeAny,
        Integer minAge,
        Integer maxAge,

        String photoUrl,

        List<String> tags,

        CompanionType companionType
) {
    public PostUpdateRequest {
        if (title != null) title = title.strip();
        if (content != null) content = content.strip();
        if (photoUrl != null) photoUrl = photoUrl.strip();
        if (tags != null) {
            tags = tags.stream()
                    .filter(t -> t != null && !t.strip().isEmpty())
                    .map(String::strip)
                    .toList();
        }
    }
}
