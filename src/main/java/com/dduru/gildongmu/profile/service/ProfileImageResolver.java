package com.dduru.gildongmu.profile.service;

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

        return switch (profile.getProfileImageType()) {
            case DEFAULT -> defaultProfileImageUrl;
            case UPLOADED -> profile.getUploadedImageUrl();
            case AVATAR -> profile.getAvatar().getImageUrl();
        };
    }

    public String resolve(ProfileImageType profileImageType, String uploadedImageUrl, String avatarImageUrl) {
        if (profileImageType == null) {
            return null;
        }

        return switch (profileImageType) {
            case DEFAULT -> defaultProfileImageUrl;
            case UPLOADED -> uploadedImageUrl;
            case AVATAR -> avatarImageUrl;
        };
    }
}
