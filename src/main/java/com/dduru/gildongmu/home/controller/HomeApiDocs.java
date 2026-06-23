package com.dduru.gildongmu.home.controller;

import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.home.dto.response.HomePopularDestinationResponse;
import com.dduru.gildongmu.home.dto.response.HomeResponse;
import com.dduru.gildongmu.home.dto.response.HomeSuperHostResponse;
import com.dduru.gildongmu.home.dto.response.MateRecommendationResponse;
import com.dduru.gildongmu.home.dto.response.SameAgeTripResponse;
import com.dduru.gildongmu.home.dto.response.SameDestinationTripResponse;
import com.dduru.gildongmu.home.dto.response.UpcomingTripResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Home", description = "홈 API")
public interface HomeApiDocs {

    @Operation(
            summary = "홈 초기 구성 조회",
            description = """
                    홈 화면 진입에 필요한 사용자 상태와 섹션 호출 정보를 조회합니다.

                    실제 섹션 데이터는 sections[].endpoint로 내려가는 API를 클라이언트가 별도로 호출합니다.
                    모든 홈 섹션 데이터는 /api/v1/home/* 홈 전용 API로 조회합니다.
                    enabled=false인 섹션은 클라이언트가 호출하지 않고 disabledReason을 기준으로 UI를 처리합니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<ApiResult<HomeResponse>> retrieveHome(
            @Parameter(hidden = true) Long userId
    );

    @Operation(summary = "홈 예정 여행 섹션 조회", description = "홈 예정 여행 섹션 데이터를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({ErrorCode.UNAUTHORIZED})
    ResponseEntity<ApiResult<UpcomingTripResponse>> retrieveUpcomingTrip(
            @Parameter(hidden = true) Long userId
    );

    @Operation(
            summary = "홈 인기 여행지 섹션 조회",
            description = "홈 인기 여행지 섹션 데이터를 조회합니다. 현재는 화면 연동을 위한 mock 데이터 5개를 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<ApiResult<HomePopularDestinationResponse>> retrievePopularDestinations();

    @Operation(summary = "홈 메이트 추천 섹션 조회", description = "홈 메이트 추천 섹션 데이터를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({ErrorCode.UNAUTHORIZED})
    ResponseEntity<ApiResult<MateRecommendationResponse>> retrieveMateRecommendations(
            @Parameter(hidden = true) Long userId
    );

    @Operation(
            summary = "홈 슈퍼호스트 섹션 조회",
            description = "홈 슈퍼호스트 섹션 데이터를 조회합니다. 현재는 화면 연동을 위한 mock 데이터 5개를 반환하며 hasLiked는 false입니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<ApiResult<List<HomeSuperHostResponse>>> retrieveSuperHosts(
            @Parameter(hidden = true) Long userId
    );

    @Operation(summary = "홈 같은 여행지 여행 섹션 조회", description = "회원의 선호 여행지와 같은 동행 섹션 데이터를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({ErrorCode.UNAUTHORIZED})
    ResponseEntity<ApiResult<List<SameDestinationTripResponse>>> retrieveSameDestinationTrips(
            @Parameter(hidden = true) Long userId
    );

    @Operation(summary = "홈 또래 여행 섹션 조회", description = "홈 또래 여행 섹션 데이터를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({ErrorCode.UNAUTHORIZED})
    ResponseEntity<ApiResult<List<SameAgeTripResponse>>> retrieveSameAgeTrips(
            @Parameter(hidden = true) Long userId
    );
}
