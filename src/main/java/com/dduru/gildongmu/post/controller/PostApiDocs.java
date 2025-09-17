package com.dduru.gildongmu.post.controller;

import com.dduru.gildongmu.post.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

@Tag(name = "Posts", description = "여행 게시글 API")
public interface PostApiDocs {

    @Operation(summary = "게시글 목록 조회", description = "필터 조건에 따라 게시글 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<PostListResponse> retrievePostsWithFilter(
            @Parameter(description = "커서 (페이징용)") Long cursor,
            @Parameter(description = "페이지 크기", example = "10") Integer size,
            @Parameter(description = "검색 키워드") String keyword,
            @Parameter(description = "시작 날짜") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료 날짜") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "선호 성별") String preferredGender,
            @Parameter(description = "선호 나이대") String preferredAge,
            @Parameter(description = "목적지 ID") Long destinationId,
            @Parameter(description = "모집 열림 여부") Boolean isRecruitOpen
    );

    @Operation(summary = "게시글 상세 조회", description = "게시글 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    ResponseEntity<PostDetailResponse> getPostDetail(
            @Parameter(description = "게시글 ID") Long postId
    );

    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    ResponseEntity<PostCreateResponse> createPost(
            @Parameter(hidden = true) Long userId,
            @Valid PostCreateRequest request
    );

    @Operation(summary = "게시글 수정", description = "게시글을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    ResponseEntity<Void> updatePost(
            @Parameter(description = "게시글 ID") Long postId,
            @Parameter(hidden = true) Long userId,
            @Valid PostUpdateRequest request
    );

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    ResponseEntity<Void> deletePost(
            @Parameter(description = "게시글 ID") Long postId,
            @Parameter(hidden = true) Long userId
    );
}
