package com.dduru.gildongmu.destination.controller;

import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.destination.dto.DestinationInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Destinations", description = "여행지 API")
public interface DestinationApiDocs {

    @Operation(
            summary = "인기 여행지 목록 조회",
            description = "최근 30일 기준 여행글 등록 수 + 조회수 + 좋아요 수를 합산해 상위 10개 여행지를 반환합니다. 집계 데이터가 없으면 추천 여행지 10개(제주도, 부산, 강릉, 후쿠오카, 오사카, 서울, 도쿄, 교토, 방콕, 다낭)를 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<ApiResult<List<DestinationInfo>>> getPopularDestinations();

    @Operation(
            summary = "여행지 검색",
            description = "도시/국가명으로 여행지를 검색합니다. keyword가 없거나 비어 있으면 인기 여행지 목록(최근 30일 집계 기준 상위 10개, 집계 없을 시 추천 10개)을 반환합니다. 검색 시 최대 20건."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<ApiResult<List<DestinationInfo>>> searchDestinations(
            @Parameter(description = "검색어 (도시, 국가명)") String keyword
    );
}
