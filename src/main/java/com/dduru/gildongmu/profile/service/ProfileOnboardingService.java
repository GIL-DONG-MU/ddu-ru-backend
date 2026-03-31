package com.dduru.gildongmu.profile.service;

import com.dduru.gildongmu.auth.exception.InvalidTokenException;
import com.dduru.gildongmu.common.jwt.JwtTokenProvider;
import com.dduru.gildongmu.onboarding.service.OnboardingService;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.dto.request.ProfileSetupRequest;
import com.dduru.gildongmu.profile.exception.InvalidBirthDateFormatException;
import com.dduru.gildongmu.profile.repository.ProfileRepository;
import com.dduru.gildongmu.verification.exception.DuplicatePhoneNumberException;
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
                throw new DuplicatePhoneNumberException();
            }
            throw e;
        }
        onboardingService.completeOnboarding(userId);
    }

    private void checkDuplicatePhoneNumber(String phoneNumber) {
        if (profileRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DuplicatePhoneNumberException();
        }
    }

    private LocalDate parseBirthDate(String birthDateString) {
        if (birthDateString == null || birthDateString.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(birthDateString.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw InvalidBirthDateFormatException.invalidFormat();
        }
    }

    private void validateVerificationToken(String verificationToken, String phoneNumber) {
        if (!jwtTokenProvider.validateVerificationToken(verificationToken, phoneNumber)) {
            throw new InvalidTokenException();
        }
    }

    private boolean isPhoneNumberDuplicateViolation(DataIntegrityViolationException e){
        return e.getMostSpecificCause().getMessage().contains("phone_number");
    }
}
