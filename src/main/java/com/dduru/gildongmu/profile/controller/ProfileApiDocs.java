package com.dduru.gildongmu.profile.controller;

import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorResponse;
import com.dduru.gildongmu.profile.dto.NicknameRandomResponse;
import com.dduru.gildongmu.profile.dto.NicknameUpdateRequest;
import com.dduru.gildongmu.profile.dto.NicknameValidateResponse;
import com.dduru.gildongmu.profile.dto.ProfileSetupRequest;
import com.dduru.gildongmu.profile.validator.ValidNickname;
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

@Tag(name = "Profiles", description = "사용자 프로필 관리 API")
public interface ProfileApiDocs {

    @Operation(summary = "닉네임 수정", description = "사용자의 닉네임을 수정합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "닉네임 수정 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 - 닉네임 형식이 올바르지 않거나 검증 규칙을 위반함",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InvalidNickname",
                                    value = """
                                            {
                                              "status": 400,
                                              "data": {
                                                "errorCode": "NICKNAME_INVALID_LENGTH",
                                                "field": "nickname",
                                                "message": "닉네임은 2자 이상 14자 이하로 입력해주세요."
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 필요 - 유효한 인증 토큰이 필요함",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "status": 401,
                                              "data": {
                                                "errorCode": "UNAUTHORIZED",
                                                "field": null,
                                                "message": "인증되지 않은 사용자입니다."
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "프로필을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "ProfileNotFound",
                                    value = """
                                            {
                                              "status": 404,
                                              "data": {
                                                "errorCode": "PROFILE_NOT_FOUND",
                                                "field": null,
                                                "message": "해당 유저의 프로필을 찾을 수 없습니다."
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "닉네임 중복 - 이미 사용 중인 닉네임임",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "NicknameAlreadyTaken",
                                    value = """
                                            {
                                              "status": 409,
                                              "data": {
                                                "errorCode": "NICKNAME_ALREADY_TAKEN",
                                                "field": null,
                                                "message": "이미 사용 중인 닉네임입니다."
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<ApiResult<Void>> updateNickname(
            @Parameter(hidden = true) Long id, 
            @Valid NicknameUpdateRequest request
    );

    @Operation(summary = "닉네임 유효성 확인", description = "닉네임의 사용 가능 여부를 확인합니다. 닉네임 형식 검증 및 중복 여부를 체크합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "닉네임 유효성 확인 성공 - 사용 가능한 닉네임임",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NicknameValidateResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 - 닉네임 형식이 올바르지 않거나 검증 규칙을 위반함 (예: 길이, 특수문자, 금지 단어 등)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InvalidNickname",
                                    value = """
                                            {
                                              "status": 400,
                                              "data": {
                                                "errorCode": "NICKNAME_INVALID_LENGTH",
                                                "field": "nickname",
                                                "message": "닉네임은 2자 이상 14자 이하로 입력해주세요."
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "닉네임 중복 - 이미 사용 중인 닉네임임",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "NicknameAlreadyTaken",
                                    value = """
                                            {
                                              "status": 409,
                                              "data": {
                                                "errorCode": "NICKNAME_ALREADY_TAKEN",
                                                "field": null,
                                                "message": "이미 사용 중인 닉네임입니다."
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<ApiResult<NicknameValidateResponse>> checkNickname(
            @Parameter(
                    description = "유효성 체크할 닉네임",
                    example = "길동무"
            )
            @ValidNickname String nickname
    );

    @Operation(summary = "랜덤 닉네임 생성", description = "랜덤으로 사용 가능한 닉네임을 생성합니다. 형용사와 명사 조합에 랜덤 숫자를 추가하여 고유성을 보장합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "랜덤 닉네임 생성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NicknameRandomResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 필요 - 유효한 인증 토큰이 필요함",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "status": 401,
                                              "data": {
                                                "errorCode": "UNAUTHORIZED",
                                                "field": null,
                                                "message": "인증되지 않은 사용자입니다."
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<ApiResult<NicknameRandomResponse>> generateRandomNickname();

    @Operation(
            summary = "프로필 초기 설정",
            description = "온보딩 과정에서 사용자의 프로필 정보를 초기 설정합니다. 닉네임, 성별, 전화번호, 생년월일을 저장하며, 비관적 잠금을 사용하여 닉네임 중복을 방지합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "프로필 초기 설정 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResult.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 - 입력 값 형식이 올바르지 않음 (예: 닉네임 형식 오류, 생년월일 형식 오류, 성별 형식 오류 등)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InvalidInput",
                                    value = """
                                            {
                                              "status": 400,
                                              "data": {
                                                "errorCode": "INVALID_INPUT_VALUE",
                                                "field": "birthday",
                                                "message": "생년월일 형식이 올바르지 않습니다. (yyyy-MM-dd 형식)"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 필요 또는 인증 토큰 오류 - 유효한 인증 토큰이 필요하거나 인증 토큰이 유효하지 않음/만료됨",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Unauthorized",
                                            value = """
                                                    {
                                                      "status": 401,
                                                      "data": {
                                                        "errorCode": "UNAUTHORIZED",
                                                        "field": null,
                                                        "message": "인증되지 않은 사용자입니다."
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "InvalidToken",
                                            value = """
                                                    {
                                                      "status": 401,
                                                      "data": {
                                                        "errorCode": "INVALID_TOKEN",
                                                        "field": null,
                                                        "message": "유효하지 않거나 만료된 인증 토큰입니다."
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "프로필을 찾을 수 없음 - 해당 사용자의 프로필이 존재하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "ProfileNotFound",
                                    value = """
                                            {
                                              "status": 404,
                                              "data": {
                                                "errorCode": "PROFILE_NOT_FOUND",
                                                "field": null,
                                                "message": "해당 유저의 프로필을 찾을 수 없습니다."
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "충돌 발생 - 닉네임 중복(NICKNAME_ALREADY_TAKEN) 또는 전화번호 중복(DUPLICATE_PHONE_NUMBER)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "NicknameAlreadyTaken",
                                            value = """
                                                    {
                                                      "status": 409,
                                                      "data": {
                                                        "errorCode": "NICKNAME_ALREADY_TAKEN",
                                                        "field": null,
                                                        "message": "이미 사용 중인 닉네임입니다."
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "DuplicatePhoneNumber",
                                            value = """
                                                    {
                                                      "status": 409,
                                                      "data": {
                                                        "errorCode": "DUPLICATE_PHONE_NUMBER",
                                                        "field": null,
                                                        "message": "이미 가입된 전화번호입니다."
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<ApiResult<Void>> setupInitialProfile(
            @Parameter(hidden = true) Long userId,
            @Parameter(
                    description = "프로필 초기 설정 요청 정보",
                    required = true
            )
            @Valid ProfileSetupRequest request
    );
}
