package com.dduru.gildongmu.nickname.controller;

import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.nickname.dto.NicknameGenerateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Nicknames", description = "닉네임 API")
public interface NicknameApiDocs {

    @Operation(
            summary = "랜덤 닉네임 생성",
            description = """
                    형용사 + 명사 조합으로 랜덤 닉네임을 생성합니다.
                    
                    ## 특징
                    - 중복되지 않는 유니크한 닉네임 생성
                    - 2자 이상 12자 이하
                    - 한글, 영어, 숫자, 공백만 허용
                    
                    ## 예시
                    - 용감한 여행자
                    - 푸른 하늘
                    - 귀여운 고양이
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "닉네임 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "status": 200,
                                              "data": {
                                                "nickname": "용감한 여행자"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResult<NicknameGenerateResponse>> generateNickname();
}
