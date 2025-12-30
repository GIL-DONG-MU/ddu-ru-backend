package com.dduru.gildongmu.verification.service;

import com.dduru.gildongmu.common.jwt.JwtTokenProvider;
import com.dduru.gildongmu.profile.repository.ProfileRepository;
import com.dduru.gildongmu.verification.dto.VerificationVerifyResponse;
import com.dduru.gildongmu.verification.exception.DuplicatePhoneNumberException;
import com.dduru.gildongmu.verification.exception.SmsSendFailedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhoneVerificationServiceTest {

    @Mock
    private SmsService smsService;
    @Mock
    private VerificationCodeService verificationCodeService;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private PhoneVerificationService phoneVerificationService;

    @Test
    void 인증번호발송_가입된번호면_예외발생() {
        // given
        when(profileRepository.existsByPhoneNumber("01012345678")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> phoneVerificationService.sendVerificationCode("01012345678"))
                .isInstanceOf(DuplicatePhoneNumberException.class);

        verify(verificationCodeService, never()).createVerification(anyString());
    }

    @Test
    void 인증번호발송_SMS실패시_롤백후_예외발생() {
        // given
        when(profileRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        VerificationCodeService.VerificationCreateResult result =
                new VerificationCodeService.VerificationCreateResult("999999", java.time.LocalDateTime.now().plusMinutes(3));
        when(verificationCodeService.createVerification("01022223333")).thenReturn(result);
        doThrow(new RuntimeException("sms fail")).when(smsService).sendSms(anyString(), anyString());

        // when & then
        assertThatThrownBy(() -> phoneVerificationService.sendVerificationCode("01022223333"))
                .isInstanceOf(SmsSendFailedException.class);

        verify(verificationCodeService).rollbackVerificationCreation("01022223333");
        verify(verificationCodeService, never()).setResendLimit(anyString());
    }

    @Test
    void 인증검증_위임및_토큰반환() {
        // given
        when(jwtTokenProvider.createVerificationToken(null, "01033334444")).thenReturn("token");

        // when
        VerificationVerifyResponse response = phoneVerificationService.verifyCode("01033334444", "123456");

        // then
        verify(verificationCodeService).verifyCode("01033334444", "123456");
        verify(jwtTokenProvider).createVerificationToken(null, "01033334444");
        org.assertj.core.api.Assertions.assertThat(response.token()).isEqualTo("token");
    }
}
