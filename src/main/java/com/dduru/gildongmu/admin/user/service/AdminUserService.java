package com.dduru.gildongmu.admin.user.service;

import com.dduru.gildongmu.admin.user.dto.response.AdminUserDetailResponse;
import com.dduru.gildongmu.admin.user.dto.response.AdminUserListResponse;
import com.dduru.gildongmu.auth.exception.UserNotFoundException;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;

    public AdminUserListResponse findAll(Pageable pageable) {
        Page<User> page = userRepository.findAllWithProfile(pageable);
        return AdminUserListResponse.from(page);
    }

    public AdminUserDetailResponse findById(Long userId) {
        User user = userRepository.findWithProfileById(userId)
                .orElseThrow(UserNotFoundException::new);
        return AdminUserDetailResponse.from(user);
    }
}
