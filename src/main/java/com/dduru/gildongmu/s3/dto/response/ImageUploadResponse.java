package com.dduru.gildongmu.s3.dto.response;

public record ImageUploadResponse(
        String presignedUrl,
        String fileUrl
) {
}
