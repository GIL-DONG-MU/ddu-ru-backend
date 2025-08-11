package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.domain.User;
import com.dduru.gildongmu.auth.exception.UserNotFoundException;
import com.dduru.gildongmu.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void updateNickname(Long userId, String nickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        user.updateNickname(nickname);
        // JPA dirty checking will automatically save the changes
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
    }
}