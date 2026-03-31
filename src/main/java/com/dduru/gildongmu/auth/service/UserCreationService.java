package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.auth.dto.UserCreationResult;
import com.dduru.gildongmu.auth.exception.DuplicateEmailException;
import com.dduru.gildongmu.auth.exception.UserCreationIntegrityException;
import com.dduru.gildongmu.onboarding.domain.UserOnboarding;
import com.dduru.gildongmu.onboarding.repository.UserOnboardingRepository;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.repository.ProfileRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.Role;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCreationService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final UserOnboardingRepository userOnboardingRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserCreationResult createNewUser(OauthUserInfo oauthUserInfo) {
        validateEmailNotDuplicate(oauthUserInfo.email());

        User user = userRepository.save(buildUser(oauthUserInfo));

        profileRepository.save(new Profile(user));

        userOnboardingRepository.save(new UserOnboarding(user));

        log.info("신규 사용자 회원가입 완료 - userId: {}, provider: {}",
                user.getId(), oauthUserInfo.loginType());

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
        .map(user -> new UserCreationResult(user, false));
    }

    private UserCreationResult handleEmailDuplicateOrThrow(OauthUserInfo oauthUserInfo) {
        if (userRepository.existsByEmail(oauthUserInfo.email())) {
            throw new DuplicateEmailException();
        }
        throw new UserCreationIntegrityException();
    }

    private void validateEmailNotDuplicate(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
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
}
