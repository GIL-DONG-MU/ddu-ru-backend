package com.dduru.gildongmu.S3.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ImageUploadRequest {
    @NotBlank
    private String fileName;
}
