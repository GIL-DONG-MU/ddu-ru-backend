package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.auth.dto.UserCreationResult;
import com.dduru.gildongmu.auth.exception.UserNotFoundException;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserDomainService {

    private final UserRepository userRepository;
    private final UserCreationService userCreationService;

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
        .orElseGet(() -> createUserOrHandleRaceCondition(oauthUserInfo));
    }

    private UserCreationResult createUserOrHandleRaceCondition(OauthUserInfo oauthUserInfo) {
        try {
            return userCreationService.createNewUser(oauthUserInfo);
        } catch (DataIntegrityViolationException e) {
            log.warn("사용자 생성 중 중복 키 위반 - oauthId: {}, oauthType: {}",
                    oauthUserInfo.oauthId(), oauthUserInfo.loginType());
            
            return userCreationService.findExistingUserOrThrow(oauthUserInfo);
        }
    }
}
