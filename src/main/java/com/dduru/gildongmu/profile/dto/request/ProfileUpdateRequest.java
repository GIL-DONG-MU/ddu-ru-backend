package com.dduru.gildongmu.profile.dto.request;

import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @Schema(description = "닉네임", example = "용감한여행자1234")
        @NotBlank(message = "닉네임은 공백일 수 없습니다.")
        String nickname,

        @Schema(description = "업로드된 프로필 이미지 URL", example = "https://example.amazonaws.com/images/uuid.png" )
        String uploadedImageUrl,

        @NotNull
        ProfileImageType profileImageType,

        Long bgColorId,

        @Size(max = 60)
        String bio
) {
    @AssertTrue(message = "AVATAR 타입에는 bgColorId가 필요합니다.")
    public boolean isBgColorIdValid() {
        return profileImageType != ProfileImageType.AVATAR || bgColorId != null;
    }

    @AssertTrue(message = "UPLOADED 타입에는 uploadedImageUrl이 필요합니다.")
    public boolean isUploadedImageUrlValid() {
        return profileImageType != ProfileImageType.UPLOADED || (uploadedImageUrl != null && !uploadedImageUrl.isBlank());
    }
}
