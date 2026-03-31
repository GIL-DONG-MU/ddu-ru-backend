package com.dduru.gildongmu.verification.service;

import com.dduru.gildongmu.common.exception.InternalServerException;
import com.dduru.gildongmu.verification.dto.VerificationData;
import com.dduru.gildongmu.verification.exception.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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
    private static final int VERIFIED_EXPIRATION_MINUTES = 10;
    private static final int MAX_VERIFICATION_ATTEMPTS = 5;
    private static final int DAILY_SMS_LIMIT = 50;
    private static final int RESEND_LIMIT_MINUTES = 1;
    private static final int DAILY_LIMIT_TTL_SECONDS = 24 * 60 * 60;
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_VERIFIED = "VERIFIED";
    private static final SecureRandom random = new SecureRandom();
    
    private DefaultRedisScript<Long> dailyLimitScript;

    /**
     * 일별 발송 제한 카운터를 관리하는 Redis Lua Script
     * KEYS[1]: Redis Key
     * ARGV[1]: 최대 시도 횟수
     * - count 증가 후 제한 초과 시: -1 반환
     * - 정상 경우: 증가된 count 반환
     */
    @PostConstruct
    public void init() {
        String script = 
            "local count = redis.call('INCR', KEYS[1])\n" +
            "if count == 1 then\n" +
            "    redis.call('EXPIRE', KEYS[1], ARGV[2])\n" +
            "end\n" +
            "if count > tonumber(ARGV[1]) then\n" +
            "    redis.call('DECR', KEYS[1])\n" +
            "    return -1\n" +
            "end\n" +
            "return count";
        
        dailyLimitScript = new DefaultRedisScript<>();
        dailyLimitScript.setScriptText(script);
        dailyLimitScript.setResultType(Long.class);
    }

    public VerificationCreateResult createVerification(String phoneNumber) {
        return createVerification(phoneNumber, false, null);
    }

    /**
     * Admin 테스트용 - 고정 인증코드 사용
     */
    public VerificationCreateResult createVerificationWithAdminCode(String phoneNumber, String adminCode) {
        return createVerification(phoneNumber, true, adminCode);
    }

    private VerificationCreateResult createVerification(String phoneNumber, boolean useAdminCode, String adminCode) {
        checkAndIncrementDailyLimit(phoneNumber);

        if (!canResend(phoneNumber)) {
            rollbackDailyLimit(phoneNumber);
            throw new ResendLimitExceededException();
        }

        String code = useAdminCode ? adminCode : generateCode();
        VerificationData data = createVerificationData(phoneNumber, code);
        saveVerificationData(phoneNumber, data);

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);
        log.info("인증번호 생성 및 저장 완료: phoneNumber={}", phoneNumber);
        
        return new VerificationCreateResult(code, expiresAt);
    }

    public void verifyCode(String phoneNumber, String inputCode) {
        String redisKey = REDIS_KEY_PREFIX + phoneNumber;
        VerificationData data = getVerificationDataFromRedis(redisKey);

        validateVerificationStatus(data);

        if (!data.code().equals(inputCode)) {
            handleInvalidCode(redisKey, data);
        }

        markAsVerified(redisKey, data, phoneNumber);
        log.info("인증번호 검증 성공: phoneNumber={}", phoneNumber);
    }

    public void rollbackVerificationCreation(String phoneNumber) {
        String authKey = REDIS_KEY_PREFIX + phoneNumber;
        String dailyLimitKey = DAILY_LIMIT_KEY_PREFIX + phoneNumber;
        
        redisTemplate.delete(authKey);
        redisTemplate.opsForValue().decrement(dailyLimitKey);
        
        log.info("인증 생성 롤백 완료: phoneNumber={}", phoneNumber);
    }

    public void setResendLimit(String phoneNumber) {
        String redisKey = RESEND_LIMIT_KEY_PREFIX + phoneNumber;
        redisTemplate.opsForValue().set(redisKey, "1", RESEND_LIMIT_MINUTES, TimeUnit.MINUTES);
    }

    private VerificationData createVerificationData(String phoneNumber, String code) {
        return VerificationData.builder()
                .code(code)
                .phone(phoneNumber)
                .count(0)
                .status(STATUS_PENDING)
                .build();
    }

    private void saveVerificationData(String phoneNumber, VerificationData data) {
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
            throw new InternalServerException();
        }
    }

    private VerificationData getVerificationDataFromRedis(String redisKey) {
        String jsonData = redisTemplate.opsForValue().get(redisKey);
        if (jsonData == null) {
            throw new VerificationNotFoundException();
        }

        try {
            return objectMapper.readValue(jsonData, VerificationData.class);
        } catch (JsonProcessingException e) {
            log.error("인증 데이터 파싱 실패: redisKey={}", redisKey, e);
            throw new VerificationNotFoundException();
        }
    }

    private void validateVerificationStatus(VerificationData data) {
        if (STATUS_VERIFIED.equals(data.status())) {
            throw new AlreadyVerifiedException();
        }
    }

    private void handleInvalidCode(String redisKey, VerificationData data) {
        int newCount = data.count() + 1;
        
        if (newCount > MAX_VERIFICATION_ATTEMPTS) {
            redisTemplate.delete(redisKey);
            throw new VerificationAttemptsExceededException();
        }
        
        incrementAttemptCount(redisKey, data);
        throw new InvalidVerificationCodeException();
    }

    private void markAsVerified(String redisKey, VerificationData data, String phoneNumber) {
        VerificationData verifiedData = VerificationData.builder()
                .code(data.code())
                .phone(data.phone())
                .count(data.count())
                .status(STATUS_VERIFIED)
                .build();

        try {
            String verifiedJson = objectMapper.writeValueAsString(verifiedData);
            redisTemplate.opsForValue().set(redisKey, verifiedJson, Duration.ofMinutes(VERIFIED_EXPIRATION_MINUTES));
        } catch (JsonProcessingException e) {
            log.error("인증 데이터 업데이트 실패: phoneNumber={}", phoneNumber, e);
            throw new InternalServerException();
        }
    }

    private void incrementAttemptCount(String redisKey, VerificationData data) {
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
            log.error("인증 데이터 업데이트 실패: redisKey={}", redisKey, e);
            throw new InternalServerException();
        }
    }

    private String generateCode() {
        int min = (int) Math.pow(10, CODE_LENGTH - 1);
        int max = (int) Math.pow(10, CODE_LENGTH) - 1;
        int code = random.nextInt(max - min + 1) + min;
        return String.valueOf(code);
    }

    private boolean canResend(String phoneNumber) {
        String redisKey = RESEND_LIMIT_KEY_PREFIX + phoneNumber;
        String value = redisTemplate.opsForValue().get(redisKey);
        return value == null;
    }

    private void checkAndIncrementDailyLimit(String phoneNumber) {
        String redisKey = DAILY_LIMIT_KEY_PREFIX + phoneNumber;
        
        List<String> keys = Collections.singletonList(redisKey);
        Long result = redisTemplate.execute(dailyLimitScript, keys, 
            String.valueOf(DAILY_SMS_LIMIT), 
            String.valueOf(DAILY_LIMIT_TTL_SECONDS));

        if (result < 0) {
            throw new DailySmsLimitExceededException();
        }
    }

    private void rollbackDailyLimit(String phoneNumber) {
        String dailyLimitKey = DAILY_LIMIT_KEY_PREFIX + phoneNumber;
        redisTemplate.opsForValue().decrement(dailyLimitKey);
        log.debug("일일 한도 롤백 완료: phoneNumber={}", phoneNumber);
    }

    public record VerificationCreateResult(String code, LocalDateTime expiresAt) {
    }
}
