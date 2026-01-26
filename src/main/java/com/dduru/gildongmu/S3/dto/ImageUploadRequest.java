package com.dduru.gildongmu.S3.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ImageUploadRequest(
        @NotEmpty(message = "파일명은 필수입니다")
        @Size(max = 10, message = "한 번에 최대 10개까지 업로드 가능합니다")
        List<@NotBlank(message = "파일명은 비어있을 수 없습니다") String> fileNames
) {
}
