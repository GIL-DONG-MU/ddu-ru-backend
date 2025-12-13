package com.dduru.gildongmu.verification.controller;

import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.verification.dto.VerificationSendRequest;
import com.dduru.gildongmu.verification.dto.VerificationSendResponse;
import com.dduru.gildongmu.verification.dto.VerificationVerifyRequest;
import com.dduru.gildongmu.verification.dto.VerificationVerifyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Phone Verification", description = "휴대폰 인증 API")
public interface PhoneVerificationApiDocs {

    @Operation(summary = "인증번호 발송", description = "전화번호로 인증번호를 발송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증번호 발송 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 전화번호 형식"),
            @ApiResponse(responseCode = "409", description = "이미 가입된 전화번호"),
            @ApiResponse(responseCode = "429", description = "재발송 제한 또는 일일 발송 한도 초과"),
            @ApiResponse(responseCode = "500", description = "SMS 발송 실패")
    })
    ResponseEntity<ApiResult<VerificationSendResponse>> sendVerificationCode(
            @Valid @RequestBody VerificationSendRequest request
    );

    @Operation(summary = "인증번호 검증", description = "발송된 인증번호를 검증하고 인증 토큰을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증번호 검증 성공"),
            @ApiResponse(responseCode = "400", description = "인증번호 불일치 또는 검증 시도 횟수 초과"),
            @ApiResponse(responseCode = "404", description = "인증 정보를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 완료된 인증")
    })
    ResponseEntity<ApiResult<VerificationVerifyResponse>> verifyCode(
            @Valid @RequestBody VerificationVerifyRequest request
    );
}
