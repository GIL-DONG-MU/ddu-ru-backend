package com.dduru.gildongmu.profile.service;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.profile.domain.BgColor;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.profile.dto.request.ProfileUpdateRequest;
import com.dduru.gildongmu.profile.repository.BgColorRepository;
import com.dduru.gildongmu.profile.repository.ProfileRepository;
import com.dduru.gildongmu.survey.domain.AvatarProfile;
import com.dduru.gildongmu.survey.repository.AvatarProfileRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProfileManagementService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final BgColorRepository bgColorRepository;
    private final AvatarProfileRepository avatarProfileRepository;

    @Transactional
    public void updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.getByIdOrThrow(userId);
        Profile profile = getProfileByUserId(user);
        BgColor bgColor = bgColorRepository.getByIdOrThrow(request.bgColorId());

        switch (request.profileImageType()) {
            case UPLOADED -> profile.updateProfile(request.uploadedImageUrl(), ProfileImageType.UPLOADED, bgColor, request.bio());
            case AVATAR -> profile.updateProfile("", ProfileImageType.AVATAR, bgColor, request.bio());
            case DEFAULT -> profile.updateProfile("", ProfileImageType.DEFAULT, bgColor, request.bio());
        }

        profileRepository.save(profile);
        log.debug("프로필 업데이트 완료: userId={}, profileImageType={}, bgColorId={}",
                userId, request.profileImageType(), request.bgColorId());
    }

    @Transactional
    public void updateAvatar(Long userId, Long avatarId) {
        User user = userRepository.getByIdOrThrow(userId);
        Profile profile = getProfileByUserId(user);
        AvatarProfile avatar = avatarProfileRepository.getByIdOrThrow(avatarId);

        profile.updateAvatar(avatar);
        log.debug("아바타 업데이트 완료: userId={}, avatarId={}", userId, avatarId);
    }

    private Profile getProfileByUserId(User user) {
        return profileRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
    }


    private ProfileImageType parseProfileImageType(String profileImageTypeString) {
        try {
            return ProfileImageType.valueOf(profileImageTypeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("알 수 없는 프로필 이미지 타입: {}. DEFAULT로 처리합니다.", profileImageTypeString);
            return ProfileImageType.DEFAULT;
        }
    }
}
