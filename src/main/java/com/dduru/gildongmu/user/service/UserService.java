package com.dduru.gildongmu.user.service;

import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.dto.UserCheckNicknameResponse;
import com.dduru.gildongmu.user.dto.UserUpdateNicknameRequest;
import com.dduru.gildongmu.user.repository.UserRepository;
import com.dduru.gildongmu.user.validator.NicknameValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final NicknameValidator nicknameValidator;

    @Transactional
    public void updateNickname(Long userId, UserUpdateNicknameRequest request) {
        User user = userRepository.getByIdOrThrow(userId);

        nicknameValidator.validate(request.nickname());
        user.updateNickname(request.nickname());
    }

    @Transactional(readOnly = true)
    public UserCheckNicknameResponse checkNickname(String nickname) {
        nicknameValidator.validate(nickname);

        return UserCheckNicknameResponse.builder()
                .sanitizedNickname(nickname)
                .build();
    }
}
