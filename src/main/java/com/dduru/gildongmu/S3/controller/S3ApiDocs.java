package com.dduru.gildongmu.S3.controller;

import com.dduru.gildongmu.S3.dto.ImageUploadRequest;
import com.dduru.gildongmu.S3.dto.ImageUploadResponse;
import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Images", description = "이미지 관리 API")
public interface S3ApiDocs {

    @Operation(summary = "이미지 업로드를 위한 Presigned URL 생성",
               description = "S3에 이미지를 직접 업로드하기 위한 Presigned URL을 생성합니다.")
    @ApiResponse(responseCode = "200", description = "Presigned URL 생성 성공",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ImageUploadResponse.class)))
    @ApiErrorResponses({
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.INVALID_FILE_EXTENSION
    })
    ResponseEntity<ApiResult<ImageUploadResponse>> prepareUpload(@RequestBody ImageUploadRequest request);
}
