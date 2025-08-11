package com.dduru.gildongmu.user.service;

import com.dduru.gildongmu.auth.exception.UserNotFoundException;
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
    public void updateNickname(Long id, UserUpdateNicknameRequest request) {
        User user = findUserById(id);

        if (!StringUtils.hasText(request.nickname())) {
            user.updateNickname(user.getName());
            return;
        }
        user.updateNickname(request.nickname());
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없습니다. userId: {}", userId);
                    return new UserNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId);
                });
    }
}
