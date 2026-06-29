package com.dduru.gildongmu.verification.controller;

import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.verification.dto.request.VerificationSendRequest;
import com.dduru.gildongmu.verification.dto.response.VerificationSendResponse;
import com.dduru.gildongmu.verification.dto.request.VerificationVerifyRequest;
import com.dduru.gildongmu.verification.dto.response.VerificationVerifyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@Tag(name = "Phone Verification", description = "휴대폰 인증 API")
@SecurityRequirement(name = "JWT")
public interface PhoneVerificationApiDocs {

    @Operation(summary = "인증번호 발송", description = "전화번호로 인증번호를 발송합니다.")
    @ApiResponse(responseCode = "200", description = "인증번호 발송 성공")
    @ApiErrorResponses({
            ErrorCode.DUPLICATE_PHONE_NUMBER,
            ErrorCode.TOO_MANY_REQUESTS,
            ErrorCode.DAILY_SMS_LIMIT_EXCEEDED,
            ErrorCode.SMS_SEND_FAILED,
            ErrorCode.SMS_PROVIDER_ERROR,
            ErrorCode.UNAUTHORIZED
    })
    ResponseEntity<ApiResult<VerificationSendResponse>> sendVerificationCode(
            @Parameter(hidden = true) Long userId,
            @Valid VerificationSendRequest request
    );

    @Operation(summary = "인증번호 검증", description = "발송된 인증번호를 검증하고 인증 토큰을 발급합니다.")
    @ApiResponse(responseCode = "200", description = "인증번호 검증 성공")
    @ApiErrorResponses({
            ErrorCode.INVALID_AUTH_CODE,
            ErrorCode.VERIFICATION_ATTEMPTS_EXCEEDED,
            ErrorCode.VERIFICATION_NOT_FOUND,
            ErrorCode.ALREADY_VERIFIED,
            ErrorCode.UNAUTHORIZED
    })
    ResponseEntity<ApiResult<VerificationVerifyResponse>> verifyCode(
            @Parameter(hidden = true) Long userId,
            @Valid VerificationVerifyRequest request
    );

    @Operation(
            summary = "[Admin] 인증번호 발송",
            description = "테스트용 - SMS 발송 없이 고정 인증코드(123456) 사용. (나중에 삭제 예정)"
    )
    @ApiResponse(responseCode = "200", description = "인증번호 발송 성공")
    @ApiErrorResponses({
            ErrorCode.DUPLICATE_PHONE_NUMBER,
            ErrorCode.TOO_MANY_REQUESTS,
            ErrorCode.DAILY_SMS_LIMIT_EXCEEDED,
            ErrorCode.UNAUTHORIZED
    })
    ResponseEntity<ApiResult<VerificationSendResponse>> sendVerificationCodeAdmin(
            @Parameter(hidden = true) Long userId,
            @Valid VerificationSendRequest request
    );
}
