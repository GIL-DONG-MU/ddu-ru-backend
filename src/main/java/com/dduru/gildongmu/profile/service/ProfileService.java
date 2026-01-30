package com.dduru.gildongmu.profile.service;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.common.jwt.JwtTokenProvider;
import com.dduru.gildongmu.profile.domain.BgColor;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.profile.dto.NicknameRandomResponse;
import com.dduru.gildongmu.profile.dto.NicknameUpdateRequest;
import com.dduru.gildongmu.profile.dto.NicknameValidateResponse;
import com.dduru.gildongmu.profile.dto.ProfileSetupRequest;
import com.dduru.gildongmu.profile.dto.request.ProfileUpdateRequest;
import com.dduru.gildongmu.profile.repository.BgColorRepository;
import com.dduru.gildongmu.profile.repository.ProfileRepository;
import com.dduru.gildongmu.profile.utils.NicknameGenerator;
import com.dduru.gildongmu.survey.domain.AvatarProfile;
import com.dduru.gildongmu.survey.repository.AvatarProfileRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProfileService {

    private static final int NICKNAME_MAX_RETRY_ATTEMPTS = 10;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final NicknameGenerator nicknameGenerator;
    private final JwtTokenProvider jwtTokenProvider;

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final BgColorRepository bgColorRepository;
    private final AvatarProfileRepository avatarProfileRepository;

    @Transactional
    public void updateNickname(Long userId, NicknameUpdateRequest request) {
        User user = userRepository.getByIdOrThrow(userId);
        Profile profile = getProfileByUserId(user);
        checkDuplicateNicknameWithLock(request.nickname());

        profile.updateNickname(request.nickname());
        profileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public NicknameValidateResponse checkNickname(String nickname) {
        checkDuplicateNickname(nickname);

        return NicknameValidateResponse.builder()
                .sanitizedNickname(nickname)
                .build();
    }

    /**
     * 중복되지 않는 유니크한 닉네임을 생성합니다.
     * 형용사 + 명사 조합에 랜덤 숫자를 추가하여 고유성을 보장합니다.
     * DB에 중복이 있으면 새로운 랜덤 숫자로 재시도합니다.
     *
     * @return 유니크한 닉네임 (예: "용감한여행자1234")
     */
    @Transactional(readOnly = true)
    public NicknameRandomResponse generateRandomNickname() {

        for (int attempt = 0; attempt < NICKNAME_MAX_RETRY_ATTEMPTS; attempt++) {
            String baseNickname = nicknameGenerator.generateBaseNickname();
            int randomNumber = nicknameGenerator.generateRandomNumber();
            String nickname = baseNickname + randomNumber;

            if (!profileRepository.existsByNickname(nickname)) {
                log.info("랜덤 닉네임 생성: {}", nickname);
                return NicknameRandomResponse.of(nickname);
            }

            log.debug("해당 닉네임이 이미 존재합니다: {}, 새로운 숫자를 부여하겠습니다.", nickname);
        }

        String fallbackNickname = "뚜비" + (System.currentTimeMillis() % 10000);
        log.warn("유니크한 닉네임 생성에 {}회 실패하여 대체 닉네임 사용: {}", NICKNAME_MAX_RETRY_ATTEMPTS, fallbackNickname);
        return NicknameRandomResponse.of(fallbackNickname);
    }

    @Transactional
    public void setupInitialProfile(Long userId, ProfileSetupRequest request) {
        User user = userRepository.getByIdOrThrow(userId);
        Profile profile = getProfileByUserId(user);

        // 비관적 잠금을 사용하여 닉네임 중복 체크 (race condition 방지)
        checkDuplicateNicknameWithLock(request.nickname());

        validateVerificationToken(request.verificationToken(), request.phoneNumber());

        if (profileRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new BusinessException(ErrorCode.DUPLICATE_PHONE_NUMBER);
        }

        LocalDate birthday = parseBirthDate(request.birthday());

        profile.setupInitialProfile(
                request.nickname(),
                Gender.valueOf(request.gender()),
                request.phoneNumber(),
                birthday
        );

        profileRepository.save(profile);
        log.info("프로필 초기 설정 완료: userId={}, nickname={}", userId, request.nickname());
    }

    @Transactional
    public void updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.getByIdOrThrow(userId);
        Profile profile = getProfileByUserId(user);
        BgColor bgColor = bgColorRepository.getByIdOrThrow(request.bgColorId());

        switch (request.profileImageType()){
            case "UPLOADED":
                profile.updateProfile(request.uploadedImageUrl(), ProfileImageType.UPLOADED, bgColor, request.bio());
                break;
            case "AVATAR":
                profile.updateProfile(null, ProfileImageType.AVATAR, bgColor, request.bio());
                break;
            default:
                log.warn("알 수 없는 프로필 이미지 타입: {}. 기존 아바타 이미지로 처리합니다.", request.profileImageType());
                profile.updateProfile(null, ProfileImageType.AVATAR, bgColor, request.bio());
        }

        profileRepository.save(profile);
        log.info("프로필 업데이트 완료: userId={}, profileImageType={}, bgColorId={}",
                userId, request.profileImageType(), request.bgColorId());
    }

    @Transactional
    public void updateAvatar(Long userId, Long avatarId) {
        User user = userRepository.getByIdOrThrow(userId);
        Profile profile = getProfileByUserId(user);
        AvatarProfile avatar = avatarProfileRepository.getByIdOrThrow(avatarId);

        profile.updateAvatar(avatar);
        profileRepository.save(profile);
        log.info("아바타 업데이트 완료: userId={}, avatarId={}", userId, avatarId);
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
        if (verificationToken == null || verificationToken.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "인증 토큰이 필요합니다.");
        }

        if (!jwtTokenProvider.validateVerificationToken(verificationToken, phoneNumber)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "유효하지 않거나 만료된 인증 토큰입니다.");
        }
    }

    private Profile getProfileByUserId(User user) {
        return profileRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
    }

    private void checkDuplicateNickname(String nickname) {
        if (profileRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_TAKEN);
        }
    }

    private void checkDuplicateNicknameWithLock(String nickname) {
        if (profileRepository.existsByNicknameWithLock(nickname)) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_TAKEN);
        }
    }
}
