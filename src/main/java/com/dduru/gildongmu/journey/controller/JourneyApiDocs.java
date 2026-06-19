package com.dduru.gildongmu.journey.controller;

import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.journey.dto.request.JourneyMemberRoleUpdateRequest;
import com.dduru.gildongmu.journey.dto.request.JourneyUpdateRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyDetailResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyMainListResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyMemberInfo;
import com.dduru.gildongmu.journey.dto.response.JourneyMemberRoleResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyUpdateResponse;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Journeys", description = "나의 여정 API")
@SecurityRequirement(name = "JWT")
public interface JourneyApiDocs {

    @Operation(
            summary = "나의 여정 메인 목록 조회",
            description = "로그인 사용자가 속한 여행 워크스페이스 목록을 조회합니다. 응답은 진행 중인 여행과 종료된 여행을 분리해서 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED
    })
    ResponseEntity<ApiResult<JourneyMainListResponse>> retrieveMyJourneys(
            @Parameter(hidden = true) Long userId
    );

    @Operation(
            summary = "나의 여정 멤버 목록 조회",
            description = "active 멤버만 접근할 수 있습니다. 호스트 → 일반 멤버 순, 합류 시간 오름차순으로 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.JOURNEY_ACCESS_DENIED
    })
    ResponseEntity<ApiResult<List<JourneyMemberInfo>>> retrieveJourneyMembers(
            @Parameter(description = "여정 ID") Long journeyId,
            @Parameter(hidden = true) Long userId
    );

    @Operation(
            summary = "나의 여정 상세 조회",
            description = "로그인 사용자가 속한 나의 여정 워크스페이스 상세 정보를 조회합니다. active journey member만 접근할 수 있습니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.JOURNEY_ACCESS_DENIED,
            ErrorCode.JOURNEY_NOT_FOUND
    })
    ResponseEntity<ApiResult<JourneyDetailResponse>> retrieveMyJourneyDetail(
            @Parameter(description = "여정 ID") Long journeyId,
            @Parameter(hidden = true) Long userId
    );

    @Operation(
            summary = "나의 여정 기본 정보 수정",
            description = "active host가 나의 여정의 제목·대표 사진·여행 날짜를 수정합니다."
    )
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.JOURNEY_ACCESS_DENIED,
            ErrorCode.JOURNEY_EMPTY_PATCH,
            ErrorCode.JOURNEY_INVALID_TITLE_LENGTH,
            ErrorCode.JOURNEY_INVALID_PHOTO_URL,
            ErrorCode.JOURNEY_INCOMPLETE_TRAVEL_DATE,
            ErrorCode.JOURNEY_INVALID_TRAVEL_DATE
    })
    ResponseEntity<ApiResult<JourneyUpdateResponse>> updateJourneyBasicInfo(
            @Parameter(description = "여정 ID") Long journeyId,
            @Parameter(hidden = true) Long userId,
            JourneyUpdateRequest request
    );

    @Operation(
            summary = "나의 여정 멤버 역할 설정",
            description = "active host가 특정 멤버의 역할 목록을 전체 교체합니다(PUT). 빈 배열 전달 시 역할이 모두 해제됩니다. CUSTOM 타입 선택 시 customRoleLabel 필수(최대 10자), 최대 5개까지 지정 가능합니다."
    )
    @ApiResponse(responseCode = "200", description = "역할 설정 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.JOURNEY_ACCESS_DENIED,
            ErrorCode.JOURNEY_MEMBER_NOT_FOUND,
            ErrorCode.JOURNEY_MEMBER_INVALID_CUSTOM_ROLE_LABEL,
            ErrorCode.JOURNEY_MEMBER_ROLE_LIMIT_EXCEEDED,
            ErrorCode.JOURNEY_MEMBER_DUPLICATE_CUSTOM_ROLE_LABEL
    })
    ResponseEntity<ApiResult<JourneyMemberRoleResponse>> updateMemberRole(
            @Parameter(description = "여정 ID") Long journeyId,
            @Parameter(description = "역할을 설정할 멤버의 사용자 ID") Long memberUserId,
            @Parameter(hidden = true) Long userId,
            JourneyMemberRoleUpdateRequest request
    );

    @Operation(
            summary = "나의 여정 멤버 내보내기",
            description = "active host가 특정 나의 여정 멤버를 내보냅니다. 멤버십을 REMOVED로 변경하고 그룹 채팅방 멤버십도 함께 제거합니다."
    )
    @ApiResponse(responseCode = "204", description = "내보내기 성공", content = @Content())
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.JOURNEY_ACCESS_DENIED,
            ErrorCode.JOURNEY_MEMBER_CANNOT_REMOVE_SELF
    })
    ResponseEntity<ApiResult<Void>> removeJourneyMember(
            @Parameter(description = "여정 ID") Long journeyId,
            @Parameter(description = "내보낼 멤버의 사용자 ID") Long memberUserId,
            @Parameter(hidden = true) Long userId
    );
}
