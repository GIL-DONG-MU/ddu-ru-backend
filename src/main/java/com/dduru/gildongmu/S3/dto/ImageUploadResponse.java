package com.dduru.gildongmu.S3.dto;

public record ImageUploadResponse(
        String presignedUrl,
        String fileUrl
) {
}
