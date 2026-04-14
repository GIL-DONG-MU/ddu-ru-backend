package com.dduru.gildongmu.profile.utils;

import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProfileImageResolver {

    @Value("${profile.default-image-url}")
    private String defaultProfileImageUrl;

    public String resolve(Profile profile) {
        if (profile == null || profile.getProfileImageType() == null) {
            return null;
        }

        return resolve(
                profile.getProfileImageType(),
                profile.getUploadedImageUrl(),
                profile.getAvatar() != null ? profile.getAvatar().getImageUrl() : null
        );
    }

    public String resolve(ProfileImageType profileImageType, String uploadedImageUrl, String avatarImageUrl) {
        if (profileImageType == null) {
            return defaultProfileImageUrl;
        }

        return switch (profileImageType) {
            case DEFAULT -> defaultProfileImageUrl;
            case UPLOADED -> hasText(uploadedImageUrl) ? uploadedImageUrl : defaultProfileImageUrl;
            case AVATAR -> hasText(avatarImageUrl) ? avatarImageUrl : defaultProfileImageUrl;
        };
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
