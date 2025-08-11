package com.dduru.gildongmu.post.dto;

import com.dduru.gildongmu.auth.domain.User;

public record UserInfo(
        Long id,
        String name,
        String profileImage,
        String gender,
        String ageRange
) {
    public static UserInfo from(User user) {
        return new UserInfo(
                user.getId(),
                user.getDisplayName(), // Use nickname if available, otherwise name
                user.getProfileImage(),
                user.getGender().name(),
                user.getAgeRange().name()
        );
    }
}
