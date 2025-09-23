package com.dduru.gildongmu.comment.controller;

import com.dduru.gildongmu.comment.dto.CommentCreateRequest;
import com.dduru.gildongmu.comment.dto.CommentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

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
}
