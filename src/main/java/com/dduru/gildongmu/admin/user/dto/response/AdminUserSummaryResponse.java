package com.dduru.gildongmu.admin.user.dto.response;

import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.Role;

import java.time.LocalDateTime;

public record AdminUserSummaryResponse(
        Long id,
        String email,
        String name,
        String nickname,
        Role role,
        LocalDateTime joinedAt
) {
    public static AdminUserSummaryResponse from(User user) {
        Profile profile = user.getProfile();
        return new AdminUserSummaryResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                profile != null ? profile.getNickname() : null,
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
