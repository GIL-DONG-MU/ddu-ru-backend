package com.dduru.gildongmu.profile.service;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.onboarding.service.OnboardingService;
import com.dduru.gildongmu.profile.domain.BgColor;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.profile.dto.request.ProfileUpdateRequest;
import com.dduru.gildongmu.profile.repository.BgColorRepository;
import com.dduru.gildongmu.profile.repository.ProfileRepository;
import com.dduru.gildongmu.survey.domain.AvatarProfile;
import com.dduru.gildongmu.survey.repository.AvatarProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProfileManagementService {

    private final ProfileRepository profileRepository;
    private final BgColorRepository bgColorRepository;
    private final AvatarProfileRepository avatarProfileRepository;
    private final OnboardingService onboardingService;

    @Value("${profile.default-image-url}")
    private String defaultProfileImageUrl;

    @Transactional
    public void updateProfile(Long userId, ProfileUpdateRequest request) {
        Profile profile = profileRepository.getByUserIdOrThrow(userId);
        BgColor bgColor = bgColorRepository.getByIdOrThrow(request.bgColorId());

        checkDuplicateNickname(profile, request.nickname());

        updateProfileBasedOnProfileImageType(profile, bgColor, request);

        onboardingService.completeProfile(userId);
        log.debug("프로필 업데이트 완료: userId={}, profileImageType={}, bgColorId={}",
                userId, request.profileImageType(), request.bgColorId());
    }

    @Transactional
    public void updateAvatar(Long userId, Long avatarId) {
        Profile profile = profileRepository.getByUserIdOrThrow(userId);
        AvatarProfile avatar = avatarProfileRepository.getByIdOrThrow(avatarId);

        profile.updateAvatar(avatar);
        log.debug("아바타 업데이트 완료: userId={}, avatarId={}", userId, avatarId);
    }

    private void updateProfileBasedOnProfileImageType(Profile profile, BgColor bgColor, ProfileUpdateRequest request) {
        switch (request.profileImageType()) {
            case UPLOADED -> profile.updateProfile(
                    request.nickname(), request.uploadedImageUrl(), ProfileImageType.UPLOADED, null, request.bio());
            case AVATAR -> profile.updateProfile(
                    request.nickname(), "", ProfileImageType.AVATAR, bgColor, request.bio());
            case DEFAULT -> profile.updateProfile(
                    request.nickname(), defaultProfileImageUrl, ProfileImageType.DEFAULT, null, request.bio()
            );
        }
    }

    private void checkDuplicateNickname (Profile profile, String nickname) {
        if (nickname.equals(profile.getNickname())) {
            return;
        }

        if (profileRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_TAKEN);
        }
    }
}
