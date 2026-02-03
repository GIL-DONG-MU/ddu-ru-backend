package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.auth.dto.UserCreationResult;
import com.dduru.gildongmu.auth.exception.DuplicateEmailException;
import com.dduru.gildongmu.auth.exception.UserNotFoundException;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.repository.ProfileRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.enums.Role;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserDomainService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    @Transactional(readOnly = true)
    public User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    public UserCreationResult findOrCreateUser(OauthUserInfo oauthUserInfo) {
        return userRepository.findByOauthIdAndOauthType(
                oauthUserInfo.oauthId(),
                oauthUserInfo.loginType()
        )
        .map(user -> new UserCreationResult(user, false))
        .orElseGet(() -> createNewUser(oauthUserInfo));
    }

    private UserCreationResult createNewUser(OauthUserInfo oauthUserInfo) {
        validateEmailNotDuplicate(oauthUserInfo.email());

        User user = saveUser(oauthUserInfo);
        saveProfile(user);

        log.info("신규 사용자 회원가입 완료 - userId: {}, provider: {}, email: {}",
                user.getId(), oauthUserInfo.loginType(), oauthUserInfo.email());

        return new UserCreationResult(user, true);
    }

    private void validateEmailNotDuplicate(String email) {
        if (userRepository.existsByEmail(email)) {
            log.warn("이미 존재하는 이메일로 다른 OAuth 제공자 가입 시도: {}", email);
            throw DuplicateEmailException.of(email);
        }
    }

    private User saveUser(OauthUserInfo oauthUserInfo) {
        User user = User.builder()
                .email(oauthUserInfo.email())
                .name(oauthUserInfo.name())
                .oauthId(oauthUserInfo.oauthId())
                .oauthType(oauthUserInfo.loginType())
                .role(Role.USER)
                .build();
        return userRepository.save(user);
    }

    private void saveProfile(User user) {
        Profile profile = Profile.builder()
                .user(user)
                .profileImage(null)
                .nickname(null)
                .gender(null)
                .phoneNumber(null)
                .birthday(null)
                .build();
        profileRepository.save(profile);
    }
}
