package com.dduru.gildongmu.S3.controller;

import com.dduru.gildongmu.S3.dto.ImageUploadRequest;
import com.dduru.gildongmu.S3.dto.ImageUploadResponse;
import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Images", description = "이미지 관리 API")
public interface S3ApiDocs {

    @Operation(
            summary = "게시글 이미지 업로드를 위한 Presigned URL 생성",
            description = "게시글 작성 시 사용할 이미지를 S3에 직접 업로드하기 위한 Presigned URL을 생성합니다. "
                    + "여러 파일을 한 번에 요청할 수 있습니다 (최대 10개). "
                    + "파일명은 UUID로 변환되어 중복을 방지합니다. "
                    + "클라이언트는 각 Presigned URL로 직접 S3에 업로드한 후, 받은 fileUrl을 게시글 생성 요청의 photoUrls에 포함합니다."
    )
    @ApiResponse(responseCode = "200", description = "Presigned URL 생성 성공")
    @ApiErrorResponses({
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.INVALID_FILE_EXTENSION
    })
    ResponseEntity<ApiResult<List<ImageUploadResponse>>> preparePostImageUpload(
            @RequestBody ImageUploadRequest request
    );

    @Operation(
            summary = "프로필 이미지 업로드를 위한 Presigned URL 생성",
            description = "사용자 프로필 이미지를 S3에 직접 업로드하기 위한 Presigned URL을 생성합니다. "
                    + "여러 파일을 한 번에 요청할 수 있습니다 (최대 10개). "
                    + "파일명은 UUID로 변환되어 중복을 방지합니다. "
                    + "클라이언트는 각 Presigned URL로 직접 S3에 업로드한 후, 받은 fileUrl을 프로필 이미지 변경 요청에 포함합니다."
    )
    @ApiResponse(responseCode = "200", description = "Presigned URL 생성 성공")
    @ApiErrorResponses({
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.INVALID_FILE_EXTENSION
    })
    ResponseEntity<ApiResult<List<ImageUploadResponse>>> prepareProfileImageUpload(
            @Valid @RequestBody ImageUploadRequest request
    );

    /*@Operation(
            summary = "설문조사 이미지 업로드를 위한 Presigned URL 생성",
            description = "설문조사 질문 이미지를 S3에 직접 업로드하기 위한 Presigned URL을 생성합니다. "
                    + "여러 파일을 한 번에 요청할 수 있습니다 (최대 10개). "
                    + "설문조사 이미지는 고정된 파일명을 사용합니다 (예: q1.png, q2.png). "
                    + "클라이언트는 각 Presigned URL로 직접 S3에 업로드한 후, 받은 fileUrl을 설문 질문에 저장합니다."
    )
    @ApiResponse(responseCode = "200", description = "Presigned URL 생성 성공")
    @ApiErrorResponses({
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.INVALID_FILE_EXTENSION
    })
    ResponseEntity<ApiResult<List<ImageUploadResponse>>> prepareSurveyImageUpload(@RequestBody ImageUploadRequest request);*/
}
