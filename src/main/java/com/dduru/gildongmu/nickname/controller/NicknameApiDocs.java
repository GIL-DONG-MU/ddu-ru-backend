package com.dduru.gildongmu.nickname.controller;

import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.nickname.dto.NicknameGenerateRequest;
import com.dduru.gildongmu.nickname.dto.NicknameGenerateResponse;
import com.dduru.gildongmu.nickname.enums.NicknameTheme;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@Tag(name = "Nicknames", description = "닉네임 생성 API")
public interface NicknameApiDocs {

    @Operation(
            summary = "랜덤 닉네임 생성",
            description = """
                    형용사 + 명사 조합으로 랜덤 닉네임을 생성합니다.
                    
                    ## 특징
                    - 다양한 테마 지원 (여행, 자연, 동물, 음식, 우주, 판타지)
                    - 한 번에 최대 10개까지 생성 가능
                    - 중복되지 않는 유니크한 닉네임 생성
                    - 선택적으로 숫자 접미사 추가 가능
                    
                    ## 닉네임 규칙
                    - 2자 이상 12자 이하
                    - 한글, 영어, 숫자, 공백만 허용
                    - 이미 사용 중인 닉네임은 제외됨
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "닉네임 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NicknameGenerateResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 응답 예시",
                                    value = """
                                            {
                                              "status": 200,
                                              "data": {
                                                "nicknames": [
                                                  "용감한 여행자",
                                                  "푸른 하늘",
                                                  "귀여운 고양이",
                                                  "달콤한 마카롱",
                                                  "반짝이는 별"
                                                ],
                                                "count": 5
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (count 범위 초과 등)"
            )
    })
    ResponseEntity<ApiResult<NicknameGenerateResponse>> generateNicknames(
            @Valid NicknameGenerateRequest request
    );

    @Operation(
            summary = "테마별 랜덤 닉네임 생성 (간편)",
            description = """
                    특정 테마로 랜덤 닉네임을 간편하게 생성합니다.
                    
                    ## 지원 테마
                    - RANDOM: 모든 테마에서 랜덤 선택
                    - TRAVEL: 여행 관련 (용감한 여행자, 자유로운 모험가 등)
                    - NATURE: 자연 관련 (푸른 하늘, 맑은 바다 등)
                    - ANIMAL: 동물 관련 (귀여운 고양이, 영리한 여우 등)
                    - FOOD: 음식 관련 (달콤한 마카롱, 맛있는 푸딩 등)
                    - SPACE: 우주 관련 (반짝이는 별, 신비로운 은하 등)
                    - FANTASY: 판타지 관련 (마법의 마법사, 전설의 기사 등)
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "닉네임 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "여행 테마 응답 예시",
                                    value = """
                                            {
                                              "status": 200,
                                              "data": {
                                                "nicknames": [
                                                  "용감한 모험가",
                                                  "자유로운 여행자",
                                                  "열정적인 탐험가",
                                                  "낭만적인 방랑자",
                                                  "활기찬 배낭객"
                                                ],
                                                "count": 5
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<ApiResult<NicknameGenerateResponse>> generateNicknamesByTheme(
            @Parameter(
                    description = "닉네임 테마",
                    example = "TRAVEL",
                    schema = @Schema(implementation = NicknameTheme.class)
            )
            NicknameTheme theme,

            @Parameter(
                    description = "생성할 닉네임 개수 (1~10)",
                    example = "5"
            )
            Integer count
    );

    @Operation(
            summary = "단일 랜덤 닉네임 생성",
            description = "중복되지 않는 유니크한 닉네임을 하나 생성합니다. 회원가입 시 기본 닉네임 자동 생성에 유용합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "닉네임 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 응답 예시",
                                    value = """
                                            {
                                              "status": 200,
                                              "data": {
                                                "nicknames": ["용감한 여행자"],
                                                "count": 1
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<ApiResult<NicknameGenerateResponse>> generateSingleNickname();

    @Operation(
            summary = "사용 가능한 테마 목록 조회",
            description = "닉네임 생성에 사용 가능한 모든 테마 목록을 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "테마 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "status": 200,
                                      "data": ["RANDOM", "TRAVEL", "NATURE", "ANIMAL", "FOOD", "SPACE", "FANTASY"]
                                    }
                                    """
                    )
            )
    )
    ResponseEntity<ApiResult<NicknameTheme[]>> getAvailableThemes();
}
