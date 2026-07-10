package com.dduru.gildongmu.user.controller;

import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.post.dto.request.MyPagePostListRequest;
import com.dduru.gildongmu.post.dto.response.MyPagePostListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;

@Tag(name = "User", description = "사용자 마이페이지 API")
public interface UserApiDocs {

    @Operation(
            summary = "내가 작성한 게시글 목록 조회",
            description = "내가 작성한 게시글 목록을 커서 기반 페이지네이션으로 조회합니다.",
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED
    })
    ResponseEntity<ApiResult<MyPagePostListResponse>> retrieveMyPosts(
            @Parameter(hidden = true) Long userId,
            @Valid @ParameterObject MyPagePostListRequest request
    );

}
