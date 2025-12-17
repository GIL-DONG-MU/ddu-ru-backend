package com.dduru.gildongmu.verification.service;

import com.dduru.gildongmu.verification.dto.VerificationData;
import com.dduru.gildongmu.verification.exception.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationCodeServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private VerificationCodeService verificationCodeService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        verificationCodeService.init();
    }

    @Test
    void 인증생성_재발송제한이면_예외발생하고_일일한도롤백() {
        // given : 일일 한도는 통과하지만 재발송 키가 존재
        when(redisTemplate.execute(any(), eq(Collections.singletonList("sms:daily:01011112222")), any(), any()))
                .thenReturn(1L);
        when(valueOperations.get("sms:resend:01011112222")).thenReturn("1");

        // when & then
        assertThatThrownBy(() -> verificationCodeService.createVerification("01011112222"))
                .isInstanceOf(ResendLimitExceededException.class);
        verify(valueOperations).decrement("sms:daily:01011112222"); // rollback
    }

    @Test
    void 인증검증_코드불일치면_예외발생하고_횟수증가() throws Exception {
        // given
        String phone = "01022223333";
        VerificationData data = VerificationData.builder()
                .code("123456")
                .phone(phone)
                .count(0)
                .status("PENDING")
                .build();
        String json = objectMapper.writeValueAsString(data);
        when(valueOperations.get("sms:auth:" + phone)).thenReturn(json);

        // when & then
        assertThatThrownBy(() -> verificationCodeService.verifyCode(phone, "000000"))
                .isInstanceOf(InvalidVerificationCodeException.class);

        verify(valueOperations).set(eq("sms:auth:" + phone), contains("\"count\":1"), any(Duration.class));
    }

    @Test
    void 인증검증_코드일치면_VERIFIED로_저장() throws Exception {
        // given
        String phone = "01033334444";
        VerificationData data = VerificationData.builder()
                .code("999999")
                .phone(phone)
                .count(2)
                .status("PENDING")
                .build();
        String json = objectMapper.writeValueAsString(data);
        when(valueOperations.get("sms:auth:" + phone)).thenReturn(json);

        // when
        verificationCodeService.verifyCode(phone, "999999");

        // then
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(eq("sms:auth:" + phone), captor.capture(), eq(Duration.ofMinutes(10)));
        String savedJson = captor.getValue();
        verify(valueOperations, never()).decrement(anyString());
        VerificationData saved = objectMapper.readValue(savedJson, VerificationData.class);
        org.assertj.core.api.Assertions.assertThat(saved.status()).isEqualTo("VERIFIED");
        org.assertj.core.api.Assertions.assertThat(saved.count()).isEqualTo(2);
    }
}
