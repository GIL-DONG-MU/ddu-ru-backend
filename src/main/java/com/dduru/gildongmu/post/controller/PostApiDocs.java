package com.dduru.gildongmu.post.controller;

import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.post.dto.request.PostCreateRequest;
import com.dduru.gildongmu.post.dto.request.PostListRequest;
import com.dduru.gildongmu.post.dto.response.PostCreateResponse;
import com.dduru.gildongmu.post.dto.response.PostDetailResponse;
import com.dduru.gildongmu.post.dto.response.PostListResponse;
import com.dduru.gildongmu.post.dto.request.PostStatusUpdateRequest;
import com.dduru.gildongmu.post.dto.request.PostUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import org.springdoc.core.annotations.ParameterObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@Tag(name = "Posts", description = "여행 게시글 API")
public interface PostApiDocs {

    @Operation(summary = "게시글 목록 조회", description = "필터 조건에 따라 게시글 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<ApiResult<PostListResponse>> retrievePosts(@ParameterObject PostListRequest request);

    @Operation(summary = "게시글 상세 조회", description = "게시글 상세 정보를 조회합니다. 응답에는 현재 사용자 기준 수정 가능 여부(canEdit)가 포함됩니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({ErrorCode.POST_NOT_FOUND})
    ResponseEntity<ApiResult<PostDetailResponse>> retrievePostDetail(
            @Parameter(description = "게시글 ID") Long postId,
            @Parameter(hidden = true) Long userId
    );

    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다.")
    @ApiResponse(responseCode = "201", description = "작성 성공")
    @ApiErrorResponses({
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.INVALID_POST_DATE,
            ErrorCode.INVALID_PREFERRED_AGE,
            ErrorCode.INVALID_POST_TAGS,
            ErrorCode.DESTINATION_NOT_FOUND,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.UNAUTHORIZED
    })
    ResponseEntity<ApiResult<PostCreateResponse>> createPost(
            @Parameter(hidden = true) Long userId,
            @Valid PostCreateRequest request
    );

    @Operation(summary = "게시글 수정", description = "게시글을 수정합니다.")
    @ApiResponse(responseCode = "204", description = "수정 성공", content = @Content())
    @ApiErrorResponses({
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.POST_NOT_FOUND,
            ErrorCode.POST_ACCESS_DENIED,
            ErrorCode.INVALID_POST_DATE,
            ErrorCode.INVALID_PREFERRED_AGE,
            ErrorCode.INVALID_POST_TAGS,
            ErrorCode.DESTINATION_NOT_FOUND,
            ErrorCode.TRAVEL_ALREADY_STARTED,
            ErrorCode.TRAVEL_ALREADY_ENDED,
            ErrorCode.RECRUIT_DEADLINE_PASSED,
            ErrorCode.INVALID_RECRUIT_CAPACITY,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.UNAUTHORIZED
    })
    ResponseEntity<ApiResult<Void>> updatePost(
            @Parameter(description = "게시글 ID") Long postId,
            @Parameter(hidden = true) Long userId,
            @Valid PostUpdateRequest request
    );

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "삭제 성공", content = @Content())
    @ApiErrorResponses({
            ErrorCode.POST_NOT_FOUND,
            ErrorCode.POST_ACCESS_DENIED,
            ErrorCode.UNAUTHORIZED
    })
    ResponseEntity<ApiResult<Void>> deletePost(
            @Parameter(description = "게시글 ID") Long postId,
            @Parameter(hidden = true) Long userId
    );

    @Operation(summary = "게시글 모집 상태 변경", description = "게시글 모집 상태를 변경합니다. (true: 모집중, false: 모집마감)")
    @ApiResponse(responseCode = "204", description = "상태 변경 성공", content = @Content())
    @ApiErrorResponses({
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.POST_NOT_FOUND,
            ErrorCode.POST_ACCESS_DENIED,
            ErrorCode.INVALID_POST_STATUS,
            ErrorCode.UNAUTHORIZED
    })
    ResponseEntity<ApiResult<Void>> updatePostStatus(
            @Parameter(description = "게시글 ID") Long postId,
            @Parameter(hidden = true) Long userId,
            @Valid PostStatusUpdateRequest request
    );

}
