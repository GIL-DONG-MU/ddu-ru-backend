package com.dduru.gildongmu.S3.dto;

import jakarta.validation.constraints.NotBlank;

public record ImageUploadRequest(
        @NotBlank
        String fileName
) {
}
