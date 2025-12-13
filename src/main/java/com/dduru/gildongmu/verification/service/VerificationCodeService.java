package com.dduru.gildongmu.verification.service;

import com.dduru.gildongmu.verification.dto.VerificationData;
import com.dduru.gildongmu.verification.exception.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String REDIS_KEY_PREFIX = "sms:auth:";
    private static final String DAILY_LIMIT_KEY_PREFIX = "sms:daily:";
    private static final String RESEND_LIMIT_KEY_PREFIX = "sms:resend:";
    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_MINUTES = 3;
    private static final int MAX_VERIFICATION_ATTEMPTS = 5;
    private static final int DAILY_SMS_LIMIT = 5;
    private static final int RESEND_LIMIT_MINUTES = 1;
    private static final SecureRandom random = new SecureRandom();

    /**
     * 인증번호 생성 및 Redis에 저장
     * @param phoneNumber 전화번호 (Redis Key로 사용)
     * @return 만료 시간
     */
    public VerificationCreateResult createVerification(String phoneNumber) {
        // 1분 내 재요청 차단
        if (!canResend(phoneNumber)) {
            throw new ResendLimitExceededException("잠시 후 다시 시도해주세요.");
        }

        // 일일 발송 한도 확인
        checkDailyLimit(phoneNumber);

        // 인증번호 생성
        String code = generateCode();
        
        // Redis에 저장 (phoneNumber를 key로 사용)
        VerificationData data = VerificationData.builder()
                .code(code)
                .phone(phoneNumber)
                .count(0)
                .status("PENDING")
                .build();

        String redisKey = REDIS_KEY_PREFIX + phoneNumber;
        try {
            String jsonData = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(
                    redisKey,
                    jsonData,
                    Duration.ofMinutes(EXPIRATION_MINUTES)
            );
        } catch (JsonProcessingException e) {
            log.error("인증 데이터 저장 실패: phoneNumber={}", phoneNumber, e);
            throw new VerificationCreationException("인증 정보 저장에 실패했습니다.");
        }

        // 재발송 제한 설정
        setResendLimit(phoneNumber);
        
        // 일일 발송 횟수 증가
        incrementDailyCount(phoneNumber);

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);
        log.info("인증번호 생성 및 저장 완료: phoneNumber={}", phoneNumber);
        
        return new VerificationCreateResult(expiresAt);
    }

    /**
     * 인증번호 검증
     * @param phoneNumber 전화번호 (Redis Key)
     * @param inputCode 입력받은 인증번호
     * @return 검증 성공 여부
     */
    public boolean verifyCode(String phoneNumber, String inputCode) {
        String redisKey = REDIS_KEY_PREFIX + phoneNumber;
        String jsonData = redisTemplate.opsForValue().get(redisKey);

        if (jsonData == null) {
            throw new VerificationNotFoundException("인증 정보를 찾을 수 없습니다.");
        }

        try {
            VerificationData data = objectMapper.readValue(jsonData, VerificationData.class);

            // 이미 완료된 인증인지 확인
            if ("VERIFIED".equals(data.status())) {
                throw new AlreadyVerifiedException("이미 완료된 인증입니다.");
            }

            // 검증 시도 횟수 확인
            if (data.count() >= MAX_VERIFICATION_ATTEMPTS) {
                // 시도 횟수 초과 시 세션 삭제
                redisTemplate.delete(redisKey);
                throw new VerificationAttemptsExceededException("검증 시도 횟수를 초과했습니다.");
            }

            // 인증번호 불일치
            if (!data.code().equals(inputCode)) {
                // 시도 횟수 증가
                VerificationData updatedData = VerificationData.builder()
                        .code(data.code())
                        .phone(data.phone())
                        .count(data.count() + 1)
                        .status(data.status())
                        .build();
                
                try {
                    String updatedJson = objectMapper.writeValueAsString(updatedData);
                    redisTemplate.opsForValue().set(redisKey, updatedJson, Duration.ofMinutes(EXPIRATION_MINUTES));
                } catch (JsonProcessingException e) {
                    log.error("인증 데이터 업데이트 실패: phoneNumber={}", phoneNumber, e);
                }
                
                throw new InvalidVerificationCodeException("인증번호가 일치하지 않습니다.");
            }

            // 인증 성공 - 상태를 VERIFIED로 변경
            VerificationData verifiedData = VerificationData.builder()
                    .code(data.code())
                    .phone(data.phone())
                    .count(data.count())
                    .status("VERIFIED")
                    .build();

            try {
                String verifiedJson = objectMapper.writeValueAsString(verifiedData);
                // 인증 완료 후 10분간 유지 (토큰 발급을 위해)
                redisTemplate.opsForValue().set(redisKey, verifiedJson, Duration.ofMinutes(10));
            } catch (JsonProcessingException e) {
                log.error("인증 데이터 업데이트 실패: phoneNumber={}", phoneNumber, e);
            }

            log.info("인증번호 검증 성공: phoneNumber={}", phoneNumber);
            return true;

        } catch (JsonProcessingException e) {
            log.error("인증 데이터 파싱 실패: phoneNumber={}", phoneNumber, e);
            throw new VerificationNotFoundException("인증 정보를 찾을 수 없습니다.");
        }
    }

    /**
     * 인증 데이터 조회
     */
    public VerificationData getVerificationData(String phoneNumber) {
        String redisKey = REDIS_KEY_PREFIX + phoneNumber;
        String jsonData = redisTemplate.opsForValue().get(redisKey);

        if (jsonData == null) {
            throw new VerificationNotFoundException("인증 정보를 찾을 수 없습니다.");
        }

        try {
            return objectMapper.readValue(jsonData, VerificationData.class);
        } catch (JsonProcessingException e) {
            log.error("인증 데이터 파싱 실패: phoneNumber={}", phoneNumber, e);
            throw new VerificationNotFoundException("인증 정보를 찾을 수 없습니다.");
        }
    }

    /**
     * 인증번호 조회 (SMS 발송용)
     */
    public String getCode(String phoneNumber) {
        VerificationData data = getVerificationData(phoneNumber);
        return data.code();
    }

    /**
     * 인증번호 생성 (6자리 숫자)
     */
    private String generateCode() {
        int min = (int) Math.pow(10, CODE_LENGTH - 1);
        int max = (int) Math.pow(10, CODE_LENGTH) - 1;
        int code = random.nextInt(max - min + 1) + min;
        return String.valueOf(code);
    }

    /**
     * 재발송 가능 여부 확인 (1분 이내 재발송 방지)
     */
    private boolean canResend(String phoneNumber) {
        String redisKey = RESEND_LIMIT_KEY_PREFIX + phoneNumber;
        String value = redisTemplate.opsForValue().get(redisKey);
        return value == null;
    }

    /**
     * 재발송 제한 시간 설정 (1분)
     */
    private void setResendLimit(String phoneNumber) {
        String redisKey = RESEND_LIMIT_KEY_PREFIX + phoneNumber;
        redisTemplate.opsForValue().set(redisKey, "1", RESEND_LIMIT_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 일일 발송 한도 확인
     */
    private void checkDailyLimit(String phoneNumber) {
        String redisKey = DAILY_LIMIT_KEY_PREFIX + phoneNumber;
        String countStr = redisTemplate.opsForValue().get(redisKey);
        
        if (countStr != null) {
            int count = Integer.parseInt(countStr);
            if (count >= DAILY_SMS_LIMIT) {
                throw new DailySmsLimitExceededException("일일 발송 한도를 초과했습니다.");
            }
        }
    }

    /**
     * 일일 발송 횟수 증가
     */
    private void incrementDailyCount(String phoneNumber) {
        String redisKey = DAILY_LIMIT_KEY_PREFIX + phoneNumber;
        String countStr = redisTemplate.opsForValue().get(redisKey);
        
        int count = countStr != null ? Integer.parseInt(countStr) : 0;
        count++;
        
        // 자정까지 남은 시간 계산 (대략 24시간)
        redisTemplate.opsForValue().set(redisKey, String.valueOf(count), 24, TimeUnit.HOURS);
    }

    public record VerificationCreateResult(LocalDateTime expiresAt) {
    }
}
