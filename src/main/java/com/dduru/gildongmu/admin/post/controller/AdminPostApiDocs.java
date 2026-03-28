package com.dduru.gildongmu.admin.post.controller;

import com.dduru.gildongmu.admin.post.dto.response.AdminPostDetailResponse;
import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Admin Posts", description = "관리자 게시글 API")
public interface AdminPostApiDocs {

    @Operation(summary = "관리자 게시글 단건 조회", description = "삭제된 게시글을 포함하여 게시글 상세를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({
            ErrorCode.POST_NOT_FOUND,
            ErrorCode.UNAUTHORIZED,
            ErrorCode.ACCESS_DENIED
    })
    ResponseEntity<ApiResult<AdminPostDetailResponse>> getPost(
            @Parameter(description = "게시글 ID") Long postId
    );

    @Operation(summary = "관리자 게시글 삭제", description = "게시글을 소프트 삭제합니다. 이미 삭제된 게시글은 성공으로 처리합니다.")
    @ApiResponse(responseCode = "204", description = "삭제 성공", content = @Content())
    @ApiErrorResponses({
            ErrorCode.POST_NOT_FOUND,
            ErrorCode.UNAUTHORIZED,
            ErrorCode.ACCESS_DENIED
    })
    ResponseEntity<ApiResult<Void>> deletePost(
            @Parameter(description = "게시글 ID") Long postId,
            @Parameter(hidden = true) Long adminUserId
    );
}
