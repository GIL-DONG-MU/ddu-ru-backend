package com.dduru.gildongmu.profile.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @Schema(description = "업로드된 프로필 이미지 URL", example = "https://example.amazonaws.com/images/uuid.png" )
        String uploadedImageUrl,

        @NotBlank
        @Pattern(regexp = "DEFAULT|AVATAR|UPLOADED", message = "프로필 이미지 타입은 DEFAULT, AVATAR, UPLOADED 중 하나여야 합니다.")
        String profileImageType,

        @NotNull
        Long bgColorId,

        @Size(max = 60)
        String bio
) {
}
