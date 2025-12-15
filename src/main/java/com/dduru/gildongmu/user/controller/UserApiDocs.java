package com.dduru.gildongmu.user.controller;

import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.user.dto.NicknameRandomResponse;
import com.dduru.gildongmu.user.dto.UserCheckNicknameResponse;
import com.dduru.gildongmu.user.dto.UserUpdateNicknameRequest;
import com.dduru.gildongmu.user.validator.ValidNickname;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@Tag(name = "Users", description = "사용자 관리 API")
public interface UserApiDocs {

    @Operation(summary = "닉네임 수정", description = "사용자의 닉네임을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    ResponseEntity<ApiResult<Void>> updateNickname(
            @Parameter(hidden = true) Long id, 
            @Valid UserUpdateNicknameRequest request
    );

    @Operation(summary = "닉네임 유효성 확인", description = "닉네임의 사용 가능 여부를 확인합니다.")
    @ApiResponse(responseCode = "200", description = "유효성 확인 성공")
    ResponseEntity<ApiResult<UserCheckNicknameResponse>> checkNickname(
            @Parameter(
                    description = "체크할 닉네임 (2~12자, 한글/영문/숫자/공백만 허용, 연속 공백 불가)",
                    example = "길동무"
            )
            @ValidNickname String nickname
    );

    @Operation(summary = "랜덤 닉네임 생성", description = "랜덤으로 닉네임을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "랜덤 닉네임 생성 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResult<NicknameRandomResponse>> generateRandomNickname();
}
