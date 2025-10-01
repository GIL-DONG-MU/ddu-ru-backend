package com.dduru.gildongmu.comment.controller;

import com.dduru.gildongmu.comment.dto.CommentCreateRequest;
import com.dduru.gildongmu.comment.dto.CommentResponse;
import com.dduru.gildongmu.comment.dto.CommentUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Comments", description = "댓글 API")
public interface CommentApiDocs {
    @Operation(summary = "댓글 작성", description = "게시글에 새로운 댓글을 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "게시글 또는 부모 댓글을 찾을 수 없음")
    })
    ResponseEntity<CommentResponse> createComment(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "게시글 ID") Long postId,
            @Valid CommentCreateRequest request
    );

    @Operation(summary = "게시글 댓글 조회", description = "특정 게시글의 모든 댓글을 조회합니다. 대댓글 포함 계층 구조로 반환됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    ResponseEntity<List<CommentResponse>> retrieveComments(
            @Parameter(description = "게시글 ID") Long postId
    );

    @Operation(summary = "댓글 삭제", description = "특정 게시글의 댓글을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "게시글 또는 댓글을 찾을 수 없음")
    })
    ResponseEntity<Void> deleteComment(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "게시글 ID") Long postId,
            @Parameter(description = "댓글 ID") Long commentId
    );

    @Operation(summary = "댓글 수정", description = "특정 게시글의 댓글을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "게시글 또는 댓글을 찾을 수 없음")
    })
    ResponseEntity<Void> updateComment(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "게시글 ID") Long postId,
            @Parameter(description = "댓글 ID") Long commentId,
            @Valid CommentUpdateRequest request
    );
}
