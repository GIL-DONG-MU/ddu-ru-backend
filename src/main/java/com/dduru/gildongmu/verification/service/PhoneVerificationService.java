package com.dduru.gildongmu.verification.service;

import com.dduru.gildongmu.common.jwt.JwtTokenProvider;
import com.dduru.gildongmu.user.repository.UserRepository;
import com.dduru.gildongmu.verification.dto.VerificationSendResponse;
import com.dduru.gildongmu.verification.dto.VerificationVerifyResponse;
import com.dduru.gildongmu.verification.exception.DuplicatePhoneNumberException;
import com.dduru.gildongmu.verification.exception.SmsProviderException;
import com.dduru.gildongmu.verification.exception.SmsSendFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private final SmsService smsService;
    private final VerificationCodeService verificationCodeService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public VerificationSendResponse sendVerificationCode(String phoneNumber) {
        validatePhoneNumber(phoneNumber);

        VerificationCodeService.VerificationCreateResult result = 
                verificationCodeService.createVerification(phoneNumber);

        try {
            sendVerificationSms(phoneNumber, result.code());
            verificationCodeService.setResendLimit(phoneNumber);
        } catch (Exception e) {
            verificationCodeService.rollbackVerificationCreation(phoneNumber);
            log.error("Verification code 메서드 처리 실패", e);
            throw new SmsSendFailedException("SMS 발송 실패 등 서비스 처리 중 문제 발생");
        }

        return VerificationSendResponse.builder()
                .expiresAt(result.expiresAt())
                .build();
    }

    public VerificationVerifyResponse verifyCode(String phoneNumber, String code) {
        verificationCodeService.verifyCode(phoneNumber, code);
        String token = jwtTokenProvider.createVerificationToken(phoneNumber);
        return VerificationVerifyResponse.verified(token);
    }

    private void validatePhoneNumber(String phoneNumber) {
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DuplicatePhoneNumberException("이미 가입된 전화번호입니다. 로그인해주세요.");
        }
    }

    private void sendVerificationSms(String phoneNumber, String code) {
        String message = createVerificationMessage(code);
        
        try {
            smsService.sendSms(phoneNumber, message);
        } catch (Exception e) {
            log.error("SMS 발송 실패: phoneNumber={}", phoneNumber, e);
            throw new SmsProviderException("SMS 발송 서비스에 일시적인 오류가 발생했습니다.");
        }
    }

    private String createVerificationMessage(String code) {
        return String.format("[뚜르] 인증번호는 [%s]입니다. 3분 내에 입력해주세요.", code);
    }
}
