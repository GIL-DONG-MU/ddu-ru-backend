package com.dduru.gildongmu.user.service;

import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.dto.UserUpdateNicknameRequest;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void updateNickname(Long userId, UserUpdateNicknameRequest request) {
        User user = userRepository.getByIdOrThrow(userId);

        if (!StringUtils.hasText(request.nickname())) {
            user.updateNickname(user.getName());
            return;
        }
        user.updateNickname(request.nickname());
    }
}
