package com.dduru.gildongmu.S3.dto.response;

public record ImageUploadResponse(
        String presignedUrl,
        String fileUrl
) {
}
