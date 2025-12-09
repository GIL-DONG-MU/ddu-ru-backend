package com.dduru.gildongmu.S3.controller;

import com.dduru.gildongmu.S3.dto.ImageUploadRequest;
import com.dduru.gildongmu.S3.dto.ImageUploadResponse;
import com.dduru.gildongmu.S3.service.S3Service;
import com.dduru.gildongmu.common.dto.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/images")
public class S3Controller implements S3ApiDocs {
    private final S3Service s3Service;

    @Override
    @PostMapping("/uploads")
    public ResponseEntity<ApiResult<ImageUploadResponse>> prepareUpload(@RequestBody ImageUploadRequest request) {
        ImageUploadResponse response = s3Service.prepareUpload(request.fileName());
        return ResponseEntity.ok(ApiResult.ok(response));
    }
}
