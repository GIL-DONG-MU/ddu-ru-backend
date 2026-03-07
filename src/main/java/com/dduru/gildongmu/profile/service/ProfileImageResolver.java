package com.dduru.gildongmu.profile.service;

import com.dduru.gildongmu.profile.domain.Profile;
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
}
