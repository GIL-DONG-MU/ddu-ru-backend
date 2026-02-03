package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.auth.dto.UserCreationResult;
import com.dduru.gildongmu.auth.exception.DuplicateEmailException;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.repository.ProfileRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.enums.Role;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCreationService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserCreationResult createNewUser(OauthUserInfo oauthUserInfo) {
        validateEmailNotDuplicate(oauthUserInfo.email());

        User user = userRepository.save(buildUser(oauthUserInfo));

        saveProfile(user);

        log.info("신규 사용자 회원가입 완료 - userId: {}, provider: {}, email: {}",
                user.getId(), oauthUserInfo.loginType(), oauthUserInfo.email());

        return new UserCreationResult(user, true);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public UserCreationResult findExistingUserOrThrow(OauthUserInfo oauthUserInfo) {
        return findExistingUserByOauthId(oauthUserInfo)
                .orElseGet(() -> handleEmailDuplicateOrThrow(oauthUserInfo));
    }

    private Optional<UserCreationResult> findExistingUserByOauthId(OauthUserInfo oauthUserInfo) {
        return userRepository.findByOauthIdAndOauthType(
                oauthUserInfo.oauthId(),
                oauthUserInfo.loginType()
        )
        .map(user -> {
            log.debug("기존 사용자 재조회 (race condition 처리) - userId: {}, provider: {}",
                    user.getId(), oauthUserInfo.loginType());
            return new UserCreationResult(user, false);
        });
    }

    private UserCreationResult handleEmailDuplicateOrThrow(OauthUserInfo oauthUserInfo) {
        if (userRepository.existsByEmail(oauthUserInfo.email())) {
            log.warn("이미 존재하는 이메일로 다른 OAuth 제공자 가입 시도: {}", oauthUserInfo.email());
            throw DuplicateEmailException.of(oauthUserInfo.email());
        }
        log.error("사용자 생성 중 예상치 못한 데이터 무결성 위반 - oauthId: {}, oauthType: {}, email: {}",
                oauthUserInfo.oauthId(), oauthUserInfo.loginType(), oauthUserInfo.email());
        throw new RuntimeException("사용자 생성 중 데이터 무결성 위반 - oauthId: " +
                oauthUserInfo.oauthId() + ", email: " + oauthUserInfo.email());
    }

    private void validateEmailNotDuplicate(String email) {
        if (userRepository.existsByEmail(email)) {
            log.warn("이미 존재하는 이메일로 다른 OAuth 제공자 가입 시도: {}", email);
            throw DuplicateEmailException.of(email);
        }
    }

    private User buildUser(OauthUserInfo oauthUserInfo) {
        return User.builder()
                .email(oauthUserInfo.email())
                .name(oauthUserInfo.name())
                .oauthId(oauthUserInfo.oauthId())
                .oauthType(oauthUserInfo.loginType())
                .role(Role.USER)
                .build();
    }

    private void saveProfile(User user) {
        Profile profile = Profile.builder()
                .user(user)
                .nickname(null)
                .gender(null)
                .phoneNumber(null)
                .birthday(null)
                .avatar(null)
                .bgColor(null)
                .uploadedImageUrl(null)
                .profileImageType(null)
                .bio(null)
                .build();
        profileRepository.save(profile);
    }
}
