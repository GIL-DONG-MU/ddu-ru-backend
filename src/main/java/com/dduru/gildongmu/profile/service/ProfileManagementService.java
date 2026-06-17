package com.dduru.gildongmu.profile.service;

import com.dduru.gildongmu.chat.event.ProfileUpdatedEvent;
import com.dduru.gildongmu.onboarding.service.OnboardingService;
import com.dduru.gildongmu.profile.domain.BgColor;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.profile.dto.request.ProfileUpdateRequest;
import com.dduru.gildongmu.profile.exception.AvatarBgColorRequiredException;
import com.dduru.gildongmu.profile.exception.NicknameAlreadyTakenException;
import com.dduru.gildongmu.profile.repository.BgColorRepository;
import com.dduru.gildongmu.profile.repository.ProfileRepository;
import com.dduru.gildongmu.survey.domain.AvatarProfile;
import com.dduru.gildongmu.survey.repository.AvatarProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProfileManagementService {

    private final ProfileRepository profileRepository;
    private final BgColorRepository bgColorRepository;
    private final AvatarProfileRepository avatarProfileRepository;
    private final OnboardingService onboardingService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void updateProfile(Long userId, ProfileUpdateRequest request) {
        Profile profile = profileRepository.getByUserIdOrThrow(userId);
        BgColor bgColor = resolveBgColor(request);

        checkDuplicateNickname(profile, request.nickname());

        updateProfileBasedOnProfileImageType(profile, bgColor, request);

        onboardingService.completeProfile(userId);
        eventPublisher.publishEvent(new ProfileUpdatedEvent(userId));
    }

    @Transactional
    public void updateAvatar(Long userId, Long avatarId) {
        Profile profile = profileRepository.getByUserIdOrThrow(userId);
        AvatarProfile avatar = avatarProfileRepository.getByIdOrThrow(avatarId);

        profile.updateAvatar(avatar);
        eventPublisher.publishEvent(new ProfileUpdatedEvent(userId));
    }

    private BgColor resolveBgColor(ProfileUpdateRequest request) {
        if (request.profileImageType() != ProfileImageType.AVATAR) {
            return null;
        }

        if (request.bgColorId() == null) {
            throw new AvatarBgColorRequiredException();
        }

        return bgColorRepository.getByIdOrThrow(request.bgColorId());
    }

    private void updateProfileBasedOnProfileImageType(Profile profile, BgColor bgColor, ProfileUpdateRequest request) {
        switch (request.profileImageType()) {
            case UPLOADED -> profile.updateProfile(
                    request.nickname(), request.uploadedImageUrl(), ProfileImageType.UPLOADED, null, request.bio());
            case AVATAR -> profile.updateProfile(
                    request.nickname(), "", ProfileImageType.AVATAR, bgColor, request.bio());
            case DEFAULT -> profile.updateProfile(
                    request.nickname(), null, ProfileImageType.DEFAULT, null, request.bio()
            );
        }
    }

    private void checkDuplicateNickname (Profile profile, String nickname) {
        if (nickname.equals(profile.getNickname())) {
            return;
        }

        if (profileRepository.existsByNickname(nickname)) {
            throw new NicknameAlreadyTakenException();
        }
    }
}
