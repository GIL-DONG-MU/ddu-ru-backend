package com.dduru.gildongmu.verification.controller;

import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorResponse;
import com.dduru.gildongmu.verification.dto.VerificationSendRequest;
import com.dduru.gildongmu.verification.dto.VerificationSendResponse;
import com.dduru.gildongmu.verification.dto.VerificationVerifyRequest;
import com.dduru.gildongmu.verification.dto.VerificationVerifyResponse;
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

@Tag(name = "Phone Verification", description = "휴대폰 인증 API")
public interface PhoneVerificationApiDocs {

    @Operation(summary = "인증번호 발송", description = "전화번호로 인증번호를 발송합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "인증번호 발송 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VerificationSendResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 가입된 전화번호",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "DuplicatePhoneNumber",
                                    value = """
                                            {
                                              "status": 409,
                                              "data": {
                                                "errorCode": "DUPLICATE_PHONE_NUMBER",
                                                "field": null,
                                                "message": "이미 가입된 전화번호입니다. 로그인해주세요."
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "재발송 제한 또는 일일 발송 한도 초과",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "ResendLimitExceeded",
                                            value = """
                                                    {
                                                      "status": 429,
                                                      "data": {
                                                        "errorCode": "TOO_MANY_REQUESTS",
                                                        "field": null,
                                                        "message": "재발송 제한 시간이 지나지 않았습니다."
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "DailyLimitExceeded",
                                            value = """
                                                    {
                                                      "status": 429,
                                                      "data": {
                                                        "errorCode": "DAILY_SMS_LIMIT_EXCEEDED",
                                                        "field": null,
                                                        "message": "일일 SMS 발송 한도를 초과했습니다."
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<ApiResult<VerificationSendResponse>> sendVerificationCode(
            @Parameter(hidden = true) Long userId,
            @Valid VerificationSendRequest request
    );

    @Operation(summary = "인증번호 검증", description = "발송된 인증번호를 검증하고 인증 토큰을 발급합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "인증번호 검증 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VerificationVerifyResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "인증번호 불일치 또는 검증 시도 횟수 초과",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "InvalidAuthCode",
                                            value = """
                                                    {
                                                      "status": 400,
                                                      "data": {
                                                        "errorCode": "INVALID_AUTH_CODE",
                                                        "field": null,
                                                        "message": "인증번호가 일치하지 않습니다."
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "AttemptsExceeded",
                                            value = """
                                                    {
                                                      "status": 400,
                                                      "data": {
                                                        "errorCode": "VERIFICATION_ATTEMPTS_EXCEEDED",
                                                        "field": null,
                                                        "message": "검증 시도 횟수를 초과했습니다."
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "인증 정보를 찾을 수 없음 (만료 포함)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "VerificationNotFound",
                                    value = """
                                            {
                                              "status": 404,
                                              "data": {
                                                "errorCode": "VERIFICATION_NOT_FOUND",
                                                "field": null,
                                                "message": "인증 정보를 찾을 수 없습니다."
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 완료된 인증",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "AlreadyVerified",
                                    value = """
                                            {
                                              "status": 409,
                                              "data": {
                                                "errorCode": "ALREADY_VERIFIED",
                                                "field": null,
                                                "message": "이미 완료된 인증입니다."
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<ApiResult<VerificationVerifyResponse>> verifyCode(
            @Parameter(hidden = true) Long userId,
            @Valid VerificationVerifyRequest request
    );
}
