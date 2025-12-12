package com.dduru.gildongmu.user.service;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.dto.UserCheckNicknameResponse;
import com.dduru.gildongmu.user.dto.UserUpdateNicknameRequest;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void updateNickname(Long userId, UserUpdateNicknameRequest request) {
        User user = userRepository.getByIdOrThrow(userId);

        String sanitizedNickname = sanitizedAndCheckDuplicateNickname(request.nickname());

        user.updateNickname(sanitizedNickname);
    }

    @Transactional(readOnly = true)
    public UserCheckNicknameResponse checkNickname(String nickname) {
        String sanitizedNickname = sanitizedAndCheckDuplicateNickname(nickname);

        return UserCheckNicknameResponse.builder()
                .sanitizedNickname(sanitizedNickname)
                .build();
    }

    private String sanitizedAndCheckDuplicateNickname(String nickname) {
        String sanitizedNickname = nickname.trim();

        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_TAKEN);
        }
        return sanitizedNickname;
    }
}
