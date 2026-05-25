package com.dduru.gildongmu.journey.controller;

import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.journey.dto.request.JourneyPostCommentCreateRequest;
import com.dduru.gildongmu.journey.dto.request.JourneyPostCommentUpdateRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyPostCommentListResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyPostCommentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Journey Post Comments", description = "나의 여정 게시글 댓글 API")
@SecurityRequirement(name = "JWT")
public interface JourneyPostCommentApiDocs {

    @Operation(
            summary = "나의 여정 게시글 댓글 목록 조회",
            description = "active journey member가 같은 여정 게시글의 댓글 목록을 최신순으로 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.JOURNEY_NOT_FOUND,
            ErrorCode.JOURNEY_ACCESS_DENIED,
            ErrorCode.JOURNEY_POST_NOT_FOUND
    })
    ResponseEntity<ApiResult<JourneyPostCommentListResponse>> retrieveComments(
            @Parameter(description = "여정 ID") Long journeyId,
            @Parameter(description = "나의 여정 게시글 ID") Long journeyPostId,
            @Parameter(description = "조회할 댓글 개수 (기본 20, 최대 50)") Integer limit,
            @Parameter(hidden = true) Long userId
    );

    @Operation(
            summary = "나의 여정 게시글 댓글 작성",
            description = "active journey member가 같은 여정 게시글에 댓글을 작성합니다."
    )
    @ApiResponse(responseCode = "201", description = "작성 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.JOURNEY_NOT_FOUND,
            ErrorCode.JOURNEY_ACCESS_DENIED,
            ErrorCode.JOURNEY_POST_NOT_FOUND,
            ErrorCode.JOURNEY_POST_COMMENT_INVALID_CONTENT
    })
    ResponseEntity<ApiResult<JourneyPostCommentResponse>> createComment(
            @Parameter(description = "여정 ID") Long journeyId,
            @Parameter(description = "나의 여정 게시글 ID") Long journeyPostId,
            @Parameter(hidden = true) Long userId,
            JourneyPostCommentCreateRequest request
    );

    @Operation(
            summary = "나의 여정 게시글 댓글 수정",
            description = "작성자 본인이 나의 여정 게시글 댓글을 수정합니다."
    )
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.JOURNEY_NOT_FOUND,
            ErrorCode.JOURNEY_ACCESS_DENIED,
            ErrorCode.JOURNEY_POST_NOT_FOUND,
            ErrorCode.JOURNEY_POST_COMMENT_NOT_FOUND,
            ErrorCode.JOURNEY_POST_COMMENT_ACCESS_DENIED,
            ErrorCode.JOURNEY_POST_COMMENT_EMPTY_PATCH,
            ErrorCode.JOURNEY_POST_COMMENT_INVALID_CONTENT
    })
    ResponseEntity<ApiResult<JourneyPostCommentResponse>> updateComment(
            @Parameter(description = "여정 ID") Long journeyId,
            @Parameter(description = "나의 여정 게시글 ID") Long journeyPostId,
            @Parameter(description = "댓글 ID") Long commentId,
            @Parameter(hidden = true) Long userId,
            JourneyPostCommentUpdateRequest request
    );

    @Operation(
            summary = "나의 여정 게시글 댓글 삭제",
            description = "작성자 본인 또는 호스트가 나의 여정 게시글 댓글을 soft delete 처리합니다."
    )
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.JOURNEY_NOT_FOUND,
            ErrorCode.JOURNEY_ACCESS_DENIED,
            ErrorCode.JOURNEY_POST_NOT_FOUND,
            ErrorCode.JOURNEY_POST_COMMENT_NOT_FOUND,
            ErrorCode.JOURNEY_POST_COMMENT_ACCESS_DENIED
    })
    ResponseEntity<ApiResult<Void>> deleteComment(
            @Parameter(description = "여정 ID") Long journeyId,
            @Parameter(description = "나의 여정 게시글 ID") Long journeyPostId,
            @Parameter(description = "댓글 ID") Long commentId,
            @Parameter(hidden = true) Long userId
    );
}
