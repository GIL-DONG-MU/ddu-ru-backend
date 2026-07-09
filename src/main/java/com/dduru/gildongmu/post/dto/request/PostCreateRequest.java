package com.dduru.gildongmu.post.dto.request;

import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record PostCreateRequest(
        @Schema(description = "여행지 ID", example = "1")
        @NotNull(message = "여행지 ID는 필수입니다")
        Long destinationId,

        @Schema(description = "게시글 제목. 앞뒤 공백은 제거됩니다.", example = "제주도 2박 3일 동행 구해요")
        @NotBlank(message = "제목은 필수입니다")
        @Size(min = 5, max = 40, message = "제목은 5자 이상 40자 이하여야 합니다")
        String title,

        @Schema(description = "게시글 본문. 앞뒤 공백은 제거됩니다.", example = "제주도 서쪽 위주로 천천히 여행하실 분을 찾습니다.")
        @NotBlank(message = "내용은 필수입니다")
        @Size(min = 20, max = 1000, message = "내용은 20자 이상 1000자 이하여야 합니다")
        String content,

        @Schema(description = "여행 시작일", example = "2026-07-10")
        @NotNull(message = "여행 시작일은 필수입니다")
        @FutureOrPresent(message = "여행 시작일은 오늘 이후여야 합니다")
        LocalDate startDate,

        @Schema(description = "여행 종료일. 시작일과 같거나 이후여야 합니다.", example = "2026-07-12")
        @NotNull(message = "여행 종료일은 필수입니다")
        @FutureOrPresent(message = "여행 종료일은 오늘 이후여야 합니다")
        LocalDate endDate,

        @Schema(description = "모집 정원. 호스트를 포함한 총 인원입니다.", example = "4")
        @NotNull(message = "모집 인원은 필수입니다")
        @Min(value = 2, message = "모집 인원은 최소 2명 이상이어야 합니다 (호스트 포함)")
        @Max(value = 10, message = "모집 인원은 최대 10명까지 가능합니다 (호스트 포함)")
        Integer recruitCapacity,

        @Schema(description = "선호 성별. U는 성별 무관입니다.", example = "U", allowableValues = {"M", "F", "U"})
        @NotNull(message = "선호하는 성별을 골라주세요")
        Gender preferredGender,

        @Schema(description = "선호 연령 무관 여부. true이면 minAge/maxAge를 보내지 않습니다.", example = "false")
        @NotNull(message = "선호 연령 설정 여부는 필수입니다")
        Boolean isAgeAny,

        @Schema(description = "선호 최소 나이. isAgeAny=false일 때 maxAge와 함께 전달합니다.", example = "20", nullable = true)
        Integer minAge,
        @Schema(description = "선호 최대 나이. isAgeAny=false일 때 minAge와 함께 전달합니다.", example = "29", nullable = true)
        Integer maxAge,

        @Schema(description = "게시글 대표 이미지 URL. 업로드 이미지가 없으면 null입니다.", example = "https://cdn.example.com/posts/cover.jpg", nullable = true)
        String photoUrl,

        @Schema(description = "게시글 태그 목록. 최대 4개까지 사용할 수 있습니다.", example = "[\"맛집\", \"힐링\"]", nullable = true)
        List<String> tags,

        @Schema(description = "동행 방식. 미지정이면 UNSPECIFIED를 전달합니다.", example = "FULL", allowableValues = {"FULL", "PARTIAL", "MEAL", "UNSPECIFIED"})
        @NotNull(message = "동행 방식은 필수입니다")
        CompanionType companionType
) {
    public PostCreateRequest {
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
