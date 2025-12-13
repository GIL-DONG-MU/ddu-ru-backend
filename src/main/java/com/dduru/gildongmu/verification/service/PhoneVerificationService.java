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

    /**
     * 인증번호 발송
     * @param phoneNumber 전화번호
     * @return 만료 시간
     */
    public VerificationSendResponse sendVerificationCode(String phoneNumber) {
        // 이미 가입된 번호인지 확인 (기획에 따라 다를 수 있음)
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DuplicatePhoneNumberException("이미 가입된 전화번호입니다. 로그인해주세요.");
        }

        // 인증번호 생성 및 저장
        VerificationCodeService.VerificationCreateResult result = 
                verificationCodeService.createVerification(phoneNumber);

        // SMS 발송
        String message = String.format("[뚜르] 인증번호는 [%s]입니다. 3분 내에 입력해주세요.", result.code());
        
        try {
            smsService.sendSms(phoneNumber, message);
        } catch (SmsSendFailedException e) {
            log.error("SMS 발송 실패: phoneNumber={}", phoneNumber, e);
            throw new SmsProviderException("SMS 발송 서비스에 일시적인 오류가 발생했습니다.");
        } catch (Exception e) {
            log.error("SMS 발송 중 예상치 못한 오류 발생: phoneNumber={}", phoneNumber, e);
            throw new SmsProviderException("SMS 발송 서비스에 일시적인 오류가 발생했습니다.");
        }

        return VerificationSendResponse.builder()
                .expiresAt(result.expiresAt())
                .build();
    }

    /**
     * 인증번호 검증 및 토큰 발급
     * @param phoneNumber 전화번호
     * @param code 인증번호
     * @return 검증 결과 및 토큰
     */
    public VerificationVerifyResponse verifyCode(String phoneNumber, String code) {
        // 인증번호 검증
        verificationCodeService.verifyCode(phoneNumber, code);

        // 검증 성공 시 토큰 발급 (임시 토큰 또는 인증 토큰)
        String token = jwtTokenProvider.createVerificationToken(phoneNumber);

        return VerificationVerifyResponse.verified(token);
    }
}
