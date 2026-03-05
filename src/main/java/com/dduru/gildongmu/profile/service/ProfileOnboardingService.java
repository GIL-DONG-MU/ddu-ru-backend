package com.dduru.gildongmu.profile.service;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.common.jwt.JwtTokenProvider;
import com.dduru.gildongmu.onboarding.service.OnboardingService;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.dto.request.ProfileSetupRequest;
import com.dduru.gildongmu.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProfileOnboardingService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final JwtTokenProvider jwtTokenProvider;
    private final ProfileRepository profileRepository;
    private final OnboardingService onboardingService;

    @Transactional
    public void setupInitialProfile(Long userId, ProfileSetupRequest request) {
        Profile profile = profileRepository.getByUserIdOrThrow(userId);

        validateVerificationToken(request.verificationToken(), request.phoneNumber());

        checkDuplicatePhoneNumber(request.phoneNumber());

        profile.setupInitialProfile(
                request.gender(),
                request.phoneNumber(),
                parseBirthDate(request.birthday())
        );

        try {
            profileRepository.save(profile);
        } catch (DataIntegrityViolationException e) {
            if (isPhoneNumberDuplicateViolation(e)) {
                log.warn("전화번호 중복으로 프로필 저장 실패: phoneNumber={}, userId={}", request.phoneNumber(), userId, e);
                throw new BusinessException(ErrorCode.DUPLICATE_PHONE_NUMBER);
            }
            throw e;
        }
        onboardingService.completeOnboarding(userId);
        log.debug("프로필 초기 설정 완료: userId={}", userId);
    }

    private void checkDuplicatePhoneNumber(String phoneNumber) {
        if (profileRepository.existsByPhoneNumber(phoneNumber)) {
            throw new BusinessException(ErrorCode.DUPLICATE_PHONE_NUMBER);
        }
    }

    private LocalDate parseBirthDate(String birthDateString) {
        if (birthDateString == null || birthDateString.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(birthDateString.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("생년월일 파싱 실패: {}", birthDateString, e);
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "생년월일 형식이 올바르지 않습니다. (yyyy-MM-dd 형식)");
        }
    }

    private void validateVerificationToken(String verificationToken, String phoneNumber) {
        if (!jwtTokenProvider.validateVerificationToken(verificationToken, phoneNumber)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    private boolean isPhoneNumberDuplicateViolation(DataIntegrityViolationException e){
        if (e.getMostSpecificCause().getMessage().contains("phone_number")) {
            return true;
        }
        return false;
    }
}
