package com.dduru.gildongmu.journey.controller;

import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.journey.dto.request.JourneyPostCreateRequest;
import com.dduru.gildongmu.journey.dto.request.JourneyPostListRequest;
import com.dduru.gildongmu.journey.dto.request.JourneyPostNoticeUpdateRequest;
import com.dduru.gildongmu.journey.dto.request.JourneyPostUpdateRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyPostListResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyPostNoticeUpdateResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyPostResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;

@Tag(name = "Journey Posts", description = "나의 여정 게시판 API")
@SecurityRequirement(name = "JWT")
public interface JourneyPostApiDocs {

    @Operation(
            summary = "나의 여정 게시글 목록 조회",
            description = "active journey member가 같은 여정의 게시글 목록을 cursor 기반으로 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.JOURNEY_NOT_FOUND,
            ErrorCode.JOURNEY_ACCESS_DENIED,
            ErrorCode.JOURNEY_POST_NOT_FOUND
    })
    ResponseEntity<ApiResult<JourneyPostListResponse>> retrievePosts(
            @Parameter(description = "여정 ID") Long journeyId,
            @Parameter(hidden = true) Long userId,
            @Valid @ParameterObject JourneyPostListRequest request
    );

    @Operation(
            summary = "나의 여정 게시글 단건 조회",
            description = "active journey member가 같은 여정의 게시글을 단건 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.JOURNEY_NOT_FOUND,
            ErrorCode.JOURNEY_ACCESS_DENIED,
            ErrorCode.JOURNEY_POST_NOT_FOUND
    })
    ResponseEntity<ApiResult<JourneyPostResponse>> retrievePost(
            @Parameter(description = "여정 ID") Long journeyId,
            @Parameter(description = "나의 여정 게시글 ID") Long journeyPostId,
            @Parameter(hidden = true) Long userId
    );

    @Operation(
            summary = "나의 여정 게시글 작성",
            description = "active journey member가 같은 여정의 게시글을 작성합니다."
    )
    @ApiResponse(responseCode = "201", description = "작성 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.JOURNEY_NOT_FOUND,
            ErrorCode.JOURNEY_ACCESS_DENIED,
            ErrorCode.JOURNEY_POST_INVALID_CONTENT,
            ErrorCode.IMAGE_URL_NOT_ALLOWED
    })
    ResponseEntity<ApiResult<JourneyPostResponse>> createPost(
            @Parameter(description = "여정 ID") Long journeyId,
            @Parameter(hidden = true) Long userId,
            JourneyPostCreateRequest request
    );

    @Operation(
            summary = "나의 여정 게시글 수정",
            description = "작성자 본인이 나의 여정 게시글을 수정합니다."
    )
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.JOURNEY_NOT_FOUND,
            ErrorCode.JOURNEY_ACCESS_DENIED,
            ErrorCode.JOURNEY_POST_NOT_FOUND,
            ErrorCode.JOURNEY_POST_ACCESS_DENIED,
            ErrorCode.JOURNEY_POST_EMPTY_PATCH,
            ErrorCode.JOURNEY_POST_INVALID_CONTENT,
            ErrorCode.IMAGE_URL_NOT_ALLOWED
    })
    ResponseEntity<ApiResult<JourneyPostResponse>> updatePost(
            @Parameter(description = "여정 ID") Long journeyId,
            @Parameter(description = "나의 여정 게시글 ID") Long journeyPostId,
            @Parameter(hidden = true) Long userId,
            JourneyPostUpdateRequest request
    );

    @Operation(
            summary = "나의 여정 게시글 공지 지정/해제",
            description = "active host가 특정 게시글의 공지 여부를 변경합니다. 공지는 여정당 최대 3개까지 설정할 수 있습니다."
    )
    @ApiResponse(responseCode = "200", description = "공지 상태 변경 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.JOURNEY_NOT_FOUND,
            ErrorCode.JOURNEY_ACCESS_DENIED,
            ErrorCode.JOURNEY_POST_NOT_FOUND,
            ErrorCode.JOURNEY_POST_NOTICE_LIMIT_EXCEEDED
    })
    ResponseEntity<ApiResult<JourneyPostNoticeUpdateResponse>> updatePostNotice(
            @Parameter(description = "여정 ID") Long journeyId,
            @Parameter(description = "나의 여정 게시글 ID") Long journeyPostId,
            @Parameter(hidden = true) Long userId,
            JourneyPostNoticeUpdateRequest request
    );

    @Operation(
            summary = "나의 여정 게시글 삭제",
            description = "작성자 본인이 나의 여정 게시글을 soft delete 처리합니다."
    )
    @ApiResponse(responseCode = "204", description = "삭제 성공", content = @Content())
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.JOURNEY_NOT_FOUND,
            ErrorCode.JOURNEY_ACCESS_DENIED,
            ErrorCode.JOURNEY_POST_NOT_FOUND,
            ErrorCode.JOURNEY_POST_ACCESS_DENIED
    })
    ResponseEntity<ApiResult<Void>> deletePost(
            @Parameter(description = "여정 ID") Long journeyId,
            @Parameter(description = "나의 여정 게시글 ID") Long journeyPostId,
            @Parameter(hidden = true) Long userId
    );
}
