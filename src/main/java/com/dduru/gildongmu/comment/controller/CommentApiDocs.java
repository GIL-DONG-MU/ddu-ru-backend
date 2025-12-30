package com.dduru.gildongmu.comment.controller;

import com.dduru.gildongmu.comment.dto.CommentCreateRequest;
import com.dduru.gildongmu.comment.dto.CommentResponse;
import com.dduru.gildongmu.comment.dto.CommentUpdateRequest;
import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Comments", description = "댓글 API")
public interface CommentApiDocs {
    @Operation(summary = "댓글 작성", description = "게시글에 새로운 댓글을 작성합니다.")
    @ApiResponse(responseCode = "201", description = "작성 성공")
    @ApiErrorResponses({
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.POST_NOT_FOUND,
            ErrorCode.COMMENT_NOT_FOUND,
            ErrorCode.INVALID_PARENT_COMMENT,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.UNAUTHORIZED
    })
    ResponseEntity<ApiResult<CommentResponse>> createComment(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "게시글 ID") Long postId,
            @Valid CommentCreateRequest request
    );

    @Operation(summary = "게시글 댓글 조회", description = "특정 게시글의 모든 댓글을 조회합니다. 대댓글 포함 계층 구조로 반환됩니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({ErrorCode.POST_NOT_FOUND})
    ResponseEntity<ApiResult<List<CommentResponse>>> retrieveComments(
            @Parameter(description = "게시글 ID") Long postId
    );

    @Operation(summary = "댓글 삭제", description = "특정 게시글의 댓글을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @ApiErrorResponses({
            ErrorCode.POST_NOT_FOUND,
            ErrorCode.COMMENT_NOT_FOUND,
            ErrorCode.COMMENT_ACCESS_DENIED,
            ErrorCode.INVALID_PARENT_COMMENT,
            ErrorCode.UNAUTHORIZED
    })
    ResponseEntity<ApiResult<Void>> deleteComment(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "게시글 ID") Long postId,
            @Parameter(description = "댓글 ID") Long commentId
    );

    @Operation(summary = "댓글 수정", description = "특정 게시글의 댓글을 수정합니다.")
    @ApiResponse(responseCode = "204", description = "수정 성공")
    @ApiErrorResponses({
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.POST_NOT_FOUND,
            ErrorCode.COMMENT_NOT_FOUND,
            ErrorCode.COMMENT_ACCESS_DENIED,
            ErrorCode.INVALID_PARENT_COMMENT,
            ErrorCode.UNAUTHORIZED
    })
    ResponseEntity<ApiResult<Void>> updateComment(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "게시글 ID") Long postId,
            @Parameter(description = "댓글 ID") Long commentId,
            @Valid CommentUpdateRequest request
    );
}
