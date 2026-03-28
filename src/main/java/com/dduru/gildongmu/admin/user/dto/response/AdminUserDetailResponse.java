package com.dduru.gildongmu.admin.user.dto.response;

import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.Role;

import java.time.LocalDateTime;

public record AdminUserDetailResponse(
        Long id,
        String email,
        String name,
        String nickname,
        Role role,
        LocalDateTime joinedAt,
        AdminUserProfileResponse profile
) {
    public static AdminUserDetailResponse from(User user) {
        Profile p = user.getProfile();
        return new AdminUserDetailResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                p != null ? p.getNickname() : null,
                user.getRole(),
                user.getCreatedAt(),
                p != null ? AdminUserProfileResponse.from(p) : null
        );
    }
}
