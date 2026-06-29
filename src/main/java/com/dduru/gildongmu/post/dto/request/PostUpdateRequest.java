package com.dduru.gildongmu.post.dto.request;

import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record PostUpdateRequest(
        @Schema(description = "변경할 여행지 ID. 미전달 시 기존 값을 유지합니다.", example = "1", nullable = true)
        Long destinationId,

        @Schema(description = "변경할 게시글 제목. 미전달 시 기존 값을 유지합니다.", example = "제주도 2박 3일 동행 구해요", nullable = true)
        @Size(min = 5, max = 40, message = "제목은 5자 이상 40자 이하여야 합니다")
        String title,

        @Schema(description = "변경할 게시글 본문. 미전달 시 기존 값을 유지합니다.", example = "제주도 서쪽 위주로 천천히 여행하실 분을 찾습니다.", nullable = true)
        @Size(min = 20, max = 1000, message = "내용은 20자 이상 1000자 이하여야 합니다")
        String content,

        @Schema(description = "변경할 여행 시작일. 미전달 시 기존 값을 유지합니다.", example = "2026-07-10", nullable = true)
        @FutureOrPresent(message = "여행 시작일은 오늘 이후여야 합니다")
        LocalDate startDate,

        @Schema(description = "변경할 여행 종료일. 미전달 시 기존 값을 유지합니다.", example = "2026-07-12", nullable = true)
        @FutureOrPresent(message = "여행 종료일은 오늘 이후여야 합니다")
        LocalDate endDate,

        @Schema(description = "변경할 모집 정원. 호스트를 포함한 총 인원입니다.", example = "4", nullable = true)
        @Min(value = 2, message = "모집 인원은 최소 2명 이상이어야 합니다 (호스트 포함)")
        @Max(value = 10, message = "모집 인원은 최대 10명까지 가능합니다 (호스트 포함)")
        Integer recruitCapacity,

        @Schema(description = "변경할 선호 성별. U는 성별 무관입니다.", example = "U", allowableValues = {"M", "F", "U"}, nullable = true)
        Gender preferredGender,

        @Schema(description = "선호 연령 무관 여부. true이면 minAge/maxAge를 비워야 합니다.", example = "false", nullable = true)
        Boolean isAgeAny,
        @Schema(description = "변경할 선호 최소 나이. isAgeAny=false일 때 maxAge와 함께 전달합니다.", example = "20", nullable = true)
        Integer minAge,
        @Schema(description = "변경할 선호 최대 나이. isAgeAny=false일 때 minAge와 함께 전달합니다.", example = "29", nullable = true)
        Integer maxAge,

        @Schema(description = "변경할 대표 이미지 URL. 미전달 시 기존 값을 유지합니다.", example = "https://cdn.example.com/posts/cover.jpg", nullable = true)
        String photoUrl,

        @Schema(description = "변경할 태그 목록. 전달하면 전체 교체됩니다.", example = "[\"맛집\", \"힐링\"]", nullable = true)
        List<String> tags,

        @Schema(description = "변경할 동행 방식", example = "FULL", allowableValues = {"FULL", "PARTIAL", "MEAL"}, nullable = true)
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
