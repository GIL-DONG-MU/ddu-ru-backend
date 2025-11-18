package com.dduru.gildongmu.like.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Posts", description = "게시글 좋아요 관련 API")
public interface PostLikeApiDocs {
    @Operation(summary = "게시글 좋아요 토글", description = "게시글에 대한 좋아요를 추가하거나 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "좋아요 토글 성공")
    ResponseEntity<Void> togglePostLike(@Parameter(hidden = true) @CurrentUser Long userId, @PathVariable Long postId);
}
