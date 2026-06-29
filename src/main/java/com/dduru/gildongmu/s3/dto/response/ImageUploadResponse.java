package com.dduru.gildongmu.s3.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ImageUploadResponse(
        @Schema(description = "클라이언트가 파일을 업로드할 S3 Presigned URL", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/path/file.jpg?X-Amz-Algorithm=...")
        String presignedUrl,
        @Schema(description = "업로드 완료 후 API에 저장할 파일 접근 URL", example = "https://cdn.example.com/images/file.jpg")
        String fileUrl
) {
}
