package com.dduru.gildongmu.S3.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImageUploadResponse {
    private String presignedUrl;
    private String fileUrl;
}
